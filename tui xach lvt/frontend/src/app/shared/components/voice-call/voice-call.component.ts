import { Component, ElementRef, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VoiceCallService } from '../../../core/services/voice-call.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-voice-call',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './voice-call.component.html',
  styleUrls: ['./voice-call.component.css']
})
export class VoiceCallComponent implements OnInit, OnDestroy {
  @ViewChild('remoteAudio') remoteAudio!: ElementRef<HTMLAudioElement>;

  status: string = 'idle';
  incomingFrom: string = '';
  private subscriptions: Subscription = new Subscription();

  constructor(private voiceCallService: VoiceCallService) {}

  ngOnInit(): void {
    this.subscriptions.add(
      this.voiceCallService.callStatus$.subscribe(status => {
        this.status = status;
        console.log('Call status updated:', status);
      })
    );

    this.subscriptions.add(
      this.voiceCallService.incomingCall$.subscribe(call => {
        this.incomingFrom = call.from;
      })
    );

    this.subscriptions.add(
      this.voiceCallService.remoteStream$.subscribe(stream => {
        if (this.remoteAudio) {
          this.remoteAudio.nativeElement.srcObject = stream;
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  acceptCall(): void {
    this.voiceCallService.acceptCall();
  }

  rejectCall(): void {
    this.voiceCallService.rejectCall();
  }

  endCall(): void {
    this.voiceCallService.endCall();
  }
}
