import { Component, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { AiService } from '../../../core/services/ai.service';
import { animate, style, transition, trigger } from '@angular/animations';

interface MessageSegment {
  type: 'text' | 'product' | 'bold';
  content: string;
  productId?: number;
}

interface Message {
  segments: MessageSegment[];
  sender: 'user' | 'ai';
  timestamp: Date;
}

@Component({
  selector: 'app-ai-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './ai-chatbot.component.html',
  styleUrl: './ai-chatbot.component.scss',
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({ transform: 'translateY(20px) scale(0.95)', opacity: 0 }),
        animate('300ms cubic-bezier(0.25, 0.46, 0.45, 0.94)', style({ transform: 'translateY(0) scale(1)', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('250ms cubic-bezier(0.25, 0.46, 0.45, 0.94)', style({ transform: 'translateY(20px) scale(0.95)', opacity: 0 }))
      ])
    ])
  ]
})
export class AiChatbotComponent implements AfterViewChecked {
  @ViewChild('scrollMe') private myScrollContainer!: ElementRef;

  isOpen = false;
  userInput = '';
  isTyping = false;
  messages: Message[] = [
    {
      segments: [{ type: 'text', content: 'Xin chào! Tôi là trợ lý ảo từ LVT. Tôi có thể giúp gì cho bạn hôm nay?' }],
      sender: 'ai',
      timestamp: new Date()
    }
  ];

  constructor(
    private aiService: AiService,
    private router: Router
  ) {}

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
  }

  sendMessage() {
    if (!this.userInput.trim() || this.isTyping) return;

    const userText = this.userInput.trim();
    this.messages.push({
      segments: [{ type: 'text', content: userText }],
      sender: 'user',
      timestamp: new Date()
    });

    this.userInput = '';
    this.isTyping = true;

    this.aiService.chat(userText).subscribe({
      next: (response) => {
        this.messages.push({
          segments: this.parseMessage(response),
          sender: 'ai',
          timestamp: new Date()
        });
        this.isTyping = false;
      },
      error: () => {
        this.messages.push({
          segments: [{ type: 'text', content: 'Rất tiếc, tôi đang gặp chút sự cố. Bạn có thể thử lại sau nhé!' }],
          sender: 'ai',
          timestamp: new Date()
        });
        this.isTyping = false;
      }
    });
  }

  parseMessage(text: string): MessageSegment[] {
    const segments: MessageSegment[] = [];
    // Combined regex for [item:id:name] and **bold**
    const regex = /(\[item:(\d+):([^\]]+)\])|(\*\*(.*?)\*\*)/g;
    let lastIndex = 0;
    let match;

    while ((match = regex.exec(text)) !== null) {
      // Preceding text
      if (match.index > lastIndex) {
        segments.push({ type: 'text', content: text.substring(lastIndex, match.index) });
      }

      if (match[1]) { // Product match: [item:id:name]
        segments.push({ 
          type: 'product', 
          productId: +match[2], 
          content: match[3] 
        });
      } else if (match[4]) { // Bold match: **text**
        segments.push({ 
          type: 'bold', 
          content: match[5] 
        });
      }
      
      lastIndex = regex.lastIndex;
    }

    if (lastIndex < text.length) {
      segments.push({ type: 'text', content: text.substring(lastIndex) });
    }

    return segments.length > 0 ? segments : [{ type: 'text', content: text }];
  }

  goToProduct(id: number) {
    this.isOpen = false;
    this.router.navigate(['/products', id]);
  }

  private scrollToBottom(): void {
    try {
      this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight;
    } catch(err) { }
  }
}
