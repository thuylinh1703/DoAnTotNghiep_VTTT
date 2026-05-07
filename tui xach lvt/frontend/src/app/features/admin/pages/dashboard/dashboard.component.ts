import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DecimalPipe } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AdminService } from '../../../../core/services/admin.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  providers: [CurrencyPipe, DecimalPipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit, AfterViewInit {
  stats: any = null;
  loading = false;
  selectedYear = new Date().getFullYear();
  years: number[] = [];
  monthlyData: any[] = [];

  constructor(private adminService: AdminService) {
    const currentYear = new Date().getFullYear();
    for (let y = currentYear; y >= currentYear - 5; y--) {
      this.years.push(y);
    }
  }

  ngOnInit(): void {
    // Initial load
  }

  ngAfterViewInit(): void {
    // Single load in AfterViewInit to ensure stability
    this.loadDashboard();
  }

  loadDashboard(year: number = this.selectedYear): void {
    this.selectedYear = year;
    this.loading = true;
    this.adminService.getDashboard(year).subscribe({
      next: (response) => {
        if (response.success) {
          this.stats = response.data;
          this.calculateMonthlyData();
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onYearChange(event: any): void {
    this.loadDashboard(+event.target.value);
  }

  calculateMonthlyData(): void {
    if (!this.stats?.monthlyRevenue) {
      this.monthlyData = [];
      return;
    }
    
    const data = Object.entries(this.stats.monthlyRevenue).map(([month, revenue]: [string, any]) => ({
      month: +month,
      revenue: +revenue,
      percentage: 0
    }));

    const maxRevenue = Math.max(...data.map(d => d.revenue), 1);
    data.forEach(d => {
      d.percentage = d.revenue > 0 ? Math.max((d.revenue / maxRevenue) * 85, 2) : 0;
    });

    this.monthlyData = data;
  }
}
