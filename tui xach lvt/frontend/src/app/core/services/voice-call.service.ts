import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import * as _SockJS from 'sockjs-client';
const SockJS: any = (_SockJS as any).default || _SockJS;
import { BehaviorSubject, Subject } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class VoiceCallService {
  private stompClient: Client | null = null;
  private peerConnection: RTCPeerConnection | null = null;
  private localStream: MediaStream | null = null;
  private remoteStream: MediaStream | null = null;

  private callStatusSubject = new BehaviorSubject<string>('idle'); // idle, calling, incoming, active
  public callStatus$ = this.callStatusSubject.asObservable();

  private incomingCallSubject = new Subject<any>();
  public incomingCall$ = this.incomingCallSubject.asObservable();

  private remoteStreamSubject = new Subject<MediaStream>();
  public remoteStream$ = this.remoteStreamSubject.asObservable();

  private currentTarget: string | null = null;

  constructor(private authService: AuthService) {
    this.initWebSocket();
  }

  private initWebSocket() {
    const user = this.authService.getUser();
    if (!user) return;

    const socketUrl = environment.apiUrl.replace('/api', '/ws');
    const socket = new SockJS(socketUrl);
    
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (msg) => console.log(msg),
      onConnect: (frame) => {
        console.log('Connected to WebSocket');
        this.stompClient?.subscribe(`/topic/call/${user.email}`, (message: IMessage) => {
          this.handleSignalingMessage(JSON.parse(message.body));
        });
      },
      onStompError: (frame) => {
        console.error('STOMP error', frame);
      }
    });

    this.stompClient.activate();
  }

  private async handleSignalingMessage(message: any) {
    console.log('Received signaling message:', message);
    const { type, from, data } = message;

    switch (type) {
      case 'call-request':
        this.currentTarget = from;
        this.incomingCallSubject.next({ from });
        this.callStatusSubject.next('incoming');
        break;
      case 'offer':
        await this.handleOffer(data, from);
        break;
      case 'answer':
        await this.handleAnswer(data);
        break;
      case 'candidate':
        await this.handleCandidate(data);
        break;
      case 'call-accepted':
        this.callStatusSubject.next('active');
        break;
      case 'call-rejected':
        this.cleanup();
        this.callStatusSubject.next('idle');
        break;
      case 'call-ended':
        this.cleanup();
        this.callStatusSubject.next('idle');
        break;
    }
  }

  public async startCall(targetEmail: string) {
    this.currentTarget = targetEmail;
    this.callStatusSubject.next('calling');
    
    this.sendSignalingMessage('call-request', targetEmail, null);
    
    await this.setupPeerConnection();
    const offer = await this.peerConnection!.createOffer();
    await this.peerConnection!.setLocalDescription(offer);
    
    this.sendSignalingMessage('offer', targetEmail, offer);
  }

  public async acceptCall() {
    if (!this.currentTarget) return;
    
    this.sendSignalingMessage('call-accepted', this.currentTarget, null);
    this.callStatusSubject.next('active');
    await this.setupPeerConnection();
  }

  public rejectCall() {
    if (!this.currentTarget) return;
    this.sendSignalingMessage('call-rejected', this.currentTarget, null);
    this.cleanup();
  }

  public endCall() {
    if (!this.currentTarget) return;
    this.sendSignalingMessage('call-ended', this.currentTarget, null);
    this.cleanup();
  }

  private async setupPeerConnection() {
    this.peerConnection = new RTCPeerConnection({
      iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
    });

    this.peerConnection.onicecandidate = (event) => {
      if (event.candidate && this.currentTarget) {
        this.sendSignalingMessage('candidate', this.currentTarget, event.candidate);
      }
    };

    this.peerConnection.ontrack = (event) => {
      this.remoteStream = event.streams[0];
      this.remoteStreamSubject.next(this.remoteStream);
    };

    this.localStream = await navigator.mediaDevices.getUserMedia({ audio: true, video: false });
    this.localStream.getTracks().forEach(track => {
      this.peerConnection!.addTrack(track, this.localStream!);
    });
  }

  private async handleOffer(offer: RTCSessionDescriptionInit, from: string) {
    this.currentTarget = from;
    if (!this.peerConnection) await this.setupPeerConnection();
    
    await this.peerConnection!.setRemoteDescription(new RTCSessionDescription(offer));
    const answer = await this.peerConnection!.createAnswer();
    await this.peerConnection!.setLocalDescription(answer);
    
    this.sendSignalingMessage('answer', from, answer);
  }

  private async handleAnswer(answer: RTCSessionDescriptionInit) {
    await this.peerConnection!.setRemoteDescription(new RTCSessionDescription(answer));
  }

  private async handleCandidate(candidate: RTCIceCandidateInit) {
    await this.peerConnection!.addIceCandidate(new RTCIceCandidate(candidate));
  }

  private sendSignalingMessage(type: string, to: string, data: any) {
    const user = this.authService.getUser();
    const message = {
      type,
      from: user.email,
      to,
      data
    };
    this.stompClient?.publish({
      destination: '/app/call',
      body: JSON.stringify(message)
    });
  }

  private cleanup() {
    this.localStream?.getTracks().forEach(track => track.stop());
    this.peerConnection?.close();
    this.peerConnection = null;
    this.localStream = null;
    this.remoteStream = null;
    this.currentTarget = null;
    this.callStatusSubject.next('idle');
  }
}
