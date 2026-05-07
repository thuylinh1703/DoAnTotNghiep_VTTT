import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../../../core/services/report.service';

@Component({
  selector: 'app-product-performance',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="p-6 space-y-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Hiệu suất sản phẩm</h1>
        <p class="text-gray-500">Theo dõi bán chạy, bán chậm và sản phẩm chưa có doanh số.</p>
      </div>

      <div class="flex gap-2 flex-wrap">
        <button type="button" (click)="tab='top'; load()" [ngClass]="btnClass('top')" class="px-3 py-1.5 rounded-lg text-sm">Top bán chạy</button>
        <button type="button" (click)="tab='slow'; load()" [ngClass]="btnClass('slow')" class="px-3 py-1.5 rounded-lg text-sm">Bán chậm</button>
        <button type="button" (click)="tab='zero'; load()" [ngClass]="btnClass('zero')" class="px-3 py-1.5 rounded-lg text-sm">Zero sales</button>
      </div>

      <div class="bg-white rounded-xl border border-gray-100 overflow-hidden">
        <table class="w-full text-left text-sm">
          <thead class="bg-gray-50 text-gray-500">
            <tr>
              <th class="px-4 py-2">Sản phẩm</th>
              <th class="px-4 py-2">Danh mục</th>
              <th class="px-4 py-2 text-right">SL bán</th>
              <th class="px-4 py-2 text-right">Doanh thu</th>
              <th class="px-4 py-2 text-right">Tồn kho</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let row of rows" class="border-t border-gray-100">
              <td class="px-4 py-2">{{ row.productName }}</td>
              <td class="px-4 py-2">{{ row.categoryName || '-' }}</td>
              <td class="px-4 py-2 text-right">{{ row.unitsSold }}</td>
              <td class="px-4 py-2 text-right">{{ row.revenue | number:'1.0-0' }}</td>
              <td class="px-4 py-2 text-right">{{ row.stock }}</td>
            </tr>
            <tr *ngIf="rows.length === 0">
              <td colspan="5" class="px-4 py-8 text-center text-gray-500">Không có dữ liệu.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class ProductPerformanceComponent implements OnInit {
  tab: 'top' | 'slow' | 'zero' = 'top';
  rows: any[] = [];

  constructor(private reportService: ReportService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    if (this.tab === 'top') {
      this.reportService.getTopSellers('month', 20).subscribe((res: any) => this.rows = res?.data || []);
      return;
    }

    if (this.tab === 'slow') {
      this.reportService.getSlowMovers(20, 3).subscribe((res: any) => this.rows = res?.data || []);
      return;
    }

    this.reportService.getZeroSales(60, 20).subscribe((res: any) => this.rows = res?.data || []);
  }

  btnClass(name: 'top' | 'slow' | 'zero'): string {
    return this.tab === name
      ? 'bg-gray-900 text-white'
      : 'bg-white text-gray-700 border border-gray-200';
  }
}
