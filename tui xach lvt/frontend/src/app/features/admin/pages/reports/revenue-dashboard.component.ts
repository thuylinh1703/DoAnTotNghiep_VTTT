import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../../../core/services/report.service';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-revenue-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="space-y-8 w-full pb-12">
      <!-- Header & Filters -->
      <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div>
          <h1 class="text-section text-[#1d1d1f]">Thống kê chi tiết hệ thống</h1>
          <p class="text-[14px] tracking-[-0.224px] text-[rgba(0,0,0,0.56)] mt-2">Báo cáo toàn diện về doanh thu, sản phẩm và hành vi khách hàng.</p>
        </div>

        <div class="flex flex-wrap gap-3 items-center bg-[#f5f5f7] p-2 rounded-2xl border border-[#d2d2d7]">
          <select [(ngModel)]="groupBy" (ngModelChange)="reload()" class="h-10 px-4 rounded-xl bg-white border-0 text-[13px] font-semibold text-[#1d1d1f] shadow-sm focus:ring-2 focus:ring-gold outline-none">
            <option value="day">Theo ngày</option>
            <option value="week">Theo tuần</option>
            <option value="month">Theo tháng</option>
          </select>
          <div class="flex items-center gap-2 bg-white px-3 py-1 rounded-xl shadow-sm border border-[#d2d2d7]">
            <input type="date" [(ngModel)]="from" (change)="reload()" class="border-0 text-[12px] font-medium outline-none" />
            <span class="text-muted">→</span>
            <input type="date" [(ngModel)]="to" (change)="reload()" class="border-0 text-[12px] font-medium outline-none" />
          </div>
          <button (click)="reload()" class="h-10 w-10 flex items-center justify-center rounded-xl bg-black text-white hover:bg-gold transition-colors">
            <i class="bi bi-arrow-clockwise"></i>
          </button>
        </div>
      </div>

      <!-- Quick Summary Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6" *ngIf="summary">
        <div class="card-apple p-6 flex flex-col justify-between">
          <span class="text-[12px] uppercase tracking-[0.1em] font-bold text-[rgba(0,0,0,0.4)]">Doanh thu hôm nay</span>
          <div class="mt-4">
            <p class="text-[28px] font-bold tracking-[-0.02em] text-[#1d1d1f]">{{ summary.today | number:'1.0-0' }}₫</p>
            <span class="text-[12px] text-green-600 font-medium">Khởi đầu tốt</span>
          </div>
        </div>
        <div class="card-apple p-6 flex flex-col justify-between">
          <span class="text-[12px] uppercase tracking-[0.1em] font-bold text-[rgba(0,0,0,0.4)]">Doanh thu tháng này</span>
          <div class="mt-4">
            <p class="text-[28px] font-bold tracking-[-0.02em] text-[#1d1d1f]">{{ summary.thisMonth | number:'1.0-0' }}₫</p>
            <span class="text-[12px] text-[rgba(0,0,0,0.56)]">Mục tiêu đạt 85%</span>
          </div>
        </div>
        <div class="card-apple p-6 flex flex-col justify-between border-l-4 border-gold">
          <span class="text-[12px] uppercase tracking-[0.1em] font-bold text-[rgba(0,0,0,0.4)]">Đơn hàng mới</span>
          <div class="mt-4">
            <p class="text-[28px] font-bold tracking-[-0.02em] text-[#1d1d1f]">{{ summary.orderCount || 0 }}</p>
            <span class="text-[12px] text-[rgba(0,0,0,0.56)]">Trong khoảng đã chọn</span>
          </div>
        </div>
        <div class="card-apple p-6 flex flex-col justify-between">
          <span class="text-[12px] uppercase tracking-[0.1em] font-bold text-[rgba(0,0,0,0.4)]">Tỷ lệ tăng trưởng</span>
          <div class="mt-4">
            <p class="text-[28px] font-bold tracking-[-0.02em]" [ngClass]="summary.yoy >= 0 ? 'text-green-600' : 'text-red-600'">
              {{ summary.yoy > 0 ? '+' : '' }}{{ summary.yoy | number:'1.0-1' }}%
            </p>
            <span class="text-[12px] text-[rgba(0,0,0,0.56)]">So với cùng kỳ năm ngoái</span>
          </div>
        </div>
      </div>

      <!-- Charts Section -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- Revenue Line Chart -->
        <div class="card-apple p-6">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-[16px] font-bold text-[#1d1d1f]">Dòng doanh thu (Line Chart)</h3>
            <span class="text-[11px] uppercase tracking-wider text-muted font-bold">Biến động theo thời gian</span>
          </div>
          <div class="h-[300px]">
            <canvas id="revenueLineChart"></canvas>
          </div>
        </div>

        <!-- Category Bar Chart -->
        <div class="card-apple p-6">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-[16px] font-bold text-[#1d1d1f]">Doanh thu theo danh mục (Bar Chart)</h3>
            <span class="text-[11px] uppercase tracking-wider text-muted font-bold">Phân tích thị phần</span>
          </div>
          <div class="h-[300px]">
            <canvas id="categoryBarChart"></canvas>
          </div>
        </div>

        <!-- Payment Pie Chart -->
        <div class="card-apple p-6">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-[16px] font-bold text-[#1d1d1f]">Cơ cấu thanh toán (Pie Chart)</h3>
            <span class="text-[11px] uppercase tracking-wider text-muted font-bold">Phương thức ưa chuộng</span>
          </div>
          <div class="h-[300px] flex justify-center">
            <canvas id="paymentPieChart"></canvas>
          </div>
        </div>

        <!-- Detailed Data Table (Small) -->
        <div class="card-apple overflow-hidden">
          <div class="px-6 py-5 border-b border-[#d2d2d7] flex justify-between items-center bg-[#f5f5f7]">
            <h3 class="text-[15px] font-bold text-[#1d1d1f]">Dữ liệu thô</h3>
            <button (click)="exportCSV()" class="text-[11px] font-bold text-gold uppercase hover:underline">Xuất Excel (CSV)</button>
          </div>
          <div class="max-h-[300px] overflow-y-auto">
             <table class="w-full text-left text-[12px]">
               <thead class="sticky top-0 bg-white shadow-sm">
                 <tr class="border-b border-[#d2d2d7]">
                   <th class="px-4 py-2">Giai đoạn</th>
                   <th class="px-4 py-2 text-right">Doanh thu</th>
                   <th class="px-4 py-2 text-right">Đơn</th>
                 </tr>
               </thead>
               <tbody>
                 <tr *ngFor="let row of series" class="border-b border-[#f5f5f7]">
                   <td class="px-4 py-2">{{ row.period }}</td>
                   <td class="px-4 py-2 text-right font-medium">{{ row.revenue | number:'1.0-0' }}₫</td>
                   <td class="px-4 py-2 text-right">{{ row.orderCount }}</td>
                 </tr>
               </tbody>
             </table>
          </div>
        </div>
      </div>

      <!-- Advanced Reports: Products -->
      <div class="grid grid-cols-1 xl:grid-cols-2 gap-8">
        <!-- Top Sellers -->
        <div class="card-apple overflow-hidden">
          <div class="px-6 py-5 border-b border-[#d2d2d7] flex items-center gap-3">
            <i class="bi bi-trophy text-gold fs-5"></i>
            <h3 class="text-[16px] font-bold text-[#1d1d1f]">Sản phẩm bán chạy nhất</h3>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead>
                <tr class="bg-[#f5f5f7] border-b border-[#d2d2d7]">
                  <th class="px-6 py-3 text-[10px] uppercase tracking-[0.1em] text-[rgba(0,0,0,0.56)] font-medium">Sản phẩm</th>
                  <th class="px-6 py-3 text-[10px] uppercase tracking-[0.1em] text-[rgba(0,0,0,0.56)] font-medium text-right">Số lượng bán</th>
                  <th class="px-6 py-3 text-[10px] uppercase tracking-[0.1em] text-[rgba(0,0,0,0.56)] font-medium text-right">Doanh thu</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-[#d2d2d7]">
                <tr *ngFor="let row of topSellers" class="hover:bg-[#f5f5f7]/50 transition-colors">
                  <td class="px-6 py-3">
                    <div class="flex items-center gap-3">
                      <img [src]="getImageUrl(row.imageUrl)" class="w-8 h-8 rounded-lg object-cover bg-[#f5f5f7]">
                      <span class="text-[13px] font-medium text-[#1d1d1f] truncate max-w-[200px]">{{ row.productName }}</span>
                    </div>
                  </td>
                  <td class="px-6 py-3 text-[13px] text-right text-[#1d1d1f] font-semibold">{{ row.unitsSold }}</td>
                  <td class="px-6 py-3 text-[13px] text-right text-gold font-bold">{{ row.revenue | number:'1.0-0' }}₫</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Inventory Warnings / Slow Movers -->
        <div class="card-apple overflow-hidden">
          <div class="px-6 py-5 border-b border-[#d2d2d7] flex items-center gap-3">
            <i class="bi bi-exclamation-triangle text-[#ff3b30] fs-5"></i>
            <h3 class="text-[16px] font-bold text-[#1d1d1f]">Cảnh báo hàng tồn & Chậm</h3>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead>
                <tr class="bg-[#f5f5f7] border-b border-[#d2d2d7]">
                  <th class="px-6 py-3 text-[10px] uppercase tracking-[0.1em] text-[rgba(0,0,0,0.56)] font-medium">Sản phẩm</th>
                  <th class="px-6 py-3 text-[10px] uppercase tracking-[0.1em] text-[rgba(0,0,0,0.56)] font-medium text-right">Kho</th>
                  <th class="px-6 py-3 text-[10px] uppercase tracking-[0.1em] text-[rgba(0,0,0,0.56)] font-medium text-right">Tình trạng</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-[#d2d2d7]">
                <tr *ngFor="let row of slowMovers" class="hover:bg-[#f5f5f7]/50 transition-colors">
                  <td class="px-6 py-3">
                    <span class="text-[13px] font-medium text-[#1d1d1f]">{{ row.name }}</span>
                  </td>
                  <td class="px-6 py-3 text-[13px] text-right text-[#1d1d1f]">{{ row.quantity }}</td>
                  <td class="px-6 py-3 text-right">
                    <span class="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded"
                          [ngClass]="row.quantity <= 10 ? 'bg-[#fff1f0] text-[#ff3b30]' : 'bg-[#fff7e6] text-[#faad14]'">
                      {{ row.quantity <= 10 ? 'Cần nhập hàng' : 'Bán chậm' }}
                    </span>
                  </td>
                </tr>
                <tr *ngIf="slowMovers.length === 0">
                  <td colspan="3" class="px-6 py-12 text-center text-[14px] text-[rgba(0,0,0,0.4)]">Kho hàng hiện tại ổn định.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `
})
export class RevenueDashboardComponent implements OnInit, AfterViewInit {
  from = '';
  to = '';
  groupBy: 'day' | 'week' | 'month' = 'day';

  summary: any;
  series: any[] = [];
  byPayment: any[] = [];
  byCategory: any[] = [];
  topSellers: any[] = [];
  slowMovers: any[] = [];
  private charts: any[] = [];

  constructor(private reportService: ReportService) {}

  ngOnInit(): void {
    const toDate = new Date();
    const fromDate = new Date();
    fromDate.setDate(toDate.getDate() - 29);
    this.from = this.toDateInput(fromDate);
    this.to = this.toDateInput(toDate);
    this.reload();
  }

  reload(): void {
    this.reportService.getRevenueSummary().subscribe((res: any) => this.summary = res?.data || null);
    
    // Series Chart (Line)
    this.reportService.getRevenueSeries({ groupBy: this.groupBy, from: this.from, to: this.to })
      .subscribe((res: any) => {
        this.series = res?.data || [];
        this.updateLineChart();
      });

    // Payment Chart (Pie)
    this.reportService.getRevenueByPaymentMethod(this.from, this.to)
      .subscribe((res: any) => {
        this.byPayment = res?.data || [];
        this.updatePieChart();
      });

    // Category Chart (Bar)
    this.reportService.getRevenueByCategory(this.from, this.to)
      .subscribe((res: any) => {
        this.byCategory = res?.data || [];
        this.updateBarChart();
      });

    this.reportService.getTopSellers('month', 10)
      .subscribe((res: any) => this.topSellers = res?.data || []);
    this.reportService.getSlowMovers(10, 10)
      .subscribe((res: any) => this.slowMovers = res?.data || []);
  }

  ngAfterViewInit(): void {
    // Small delay to ensure DOM and Chart.js are ready
    setTimeout(() => {
      this.updateAllCharts();
    }, 500);
  }

  updateAllCharts(): void {
    if (this.series.length) this.updateLineChart();
    if (this.byPayment.length) this.updatePieChart();
    if (this.byCategory.length) this.updateBarChart();
  }

  updateLineChart(): void {
    this.initChart('revenueLineChart', 'line', {
      labels: this.series.map(s => s.period),
      datasets: [{
        label: 'Doanh thu (₫)',
        data: this.series.map(s => s.revenue),
        borderColor: '#c5a059',
        backgroundColor: 'rgba(197, 160, 89, 0.1)',
        fill: true,
        tension: 0.4
      }]
    });
  }

  updatePieChart(): void {
    this.initChart('paymentPieChart', 'pie', {
      labels: this.byPayment.map(p => p.key),
      datasets: [{
        data: this.byPayment.map(p => p.revenue),
        backgroundColor: ['#1d1d1f', '#c5a059', '#d2d2d7', '#f5f5f7']
      }]
    }, { plugins: { legend: { position: 'right' } } });
  }

  updateBarChart(): void {
    this.initChart('categoryBarChart', 'bar', {
      labels: this.byCategory.map(c => c.key),
      datasets: [{
        label: 'Doanh thu',
        data: this.byCategory.map(c => c.revenue),
        backgroundColor: '#1d1d1f'
      }]
    });
  }

  private initChart(id: string, type: string, data: any, options: any = {}): void {
    const ctx = document.getElementById(id) as HTMLCanvasElement;
    if (!ctx) return;
    
    // Destroy existing chart to avoid overlay
    const existing = (window as any).Chart.getChart(id);
    if (existing) existing.destroy();

    new (window as any).Chart(ctx, {
      type,
      data,
      options: {
        responsive: true,
        maintainAspectRatio: false,
        ...options
      }
    });
  }

  exportCSV(): void {
    let csv = 'Giai doan,Doanh thu,Don hang\n';
    this.series.forEach(s => {
      csv += `${s.period},${s.revenue},${s.orderCount}\n`;
    });
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `report_${this.from}_${this.to}.csv`;
    a.click();
  }

  private toDateInput(date: Date): string {
    const d = new Date(date);
    d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
    return d.toISOString().slice(0, 10);
  }

  getImageUrl(path: string): string {
    if (!path) return '';
    if (path.startsWith('http') || path.startsWith('data:')) return path;
    const baseUrl = environment.apiUrl.replace('/api', '');
    return baseUrl + (path.startsWith('/') ? '' : '/') + path;
  }
}
