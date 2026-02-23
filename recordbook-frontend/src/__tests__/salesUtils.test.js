import { filterSales, sortSales } from '../utils/salesUtils';

const sample = [
  { saleDate: '2023-01-01', quantity: 5, totalRevenue: 500, variant: 'Lemon', size: '1' },
  { saleDate: '2023-02-01', quantity: 2, totalRevenue: 200, variant: 'Neem', size: '500' },
  { saleDate: '2023-01-15', quantity: 10, totalRevenue: 1000, variant: 'Lemon', size: '5' },
];

test('filterSales filters by variant and size', () => {
  expect(filterSales(sample, { variant: 'Lemon' }).length).toBe(2);
  expect(filterSales(sample, { size: '500' }).length).toBe(1);
  expect(filterSales(sample, { variant: 'Lemon', size: '5' }).length).toBe(1);
});

test('sortSales sorts by quantity and revenue and date', () => {
  const byQtyAsc = sortSales(sample, 'quantity', 'asc');
  expect(byQtyAsc[0].quantity).toBe(2);

  const byRevenueDesc = sortSales(sample, 'revenue', 'desc');
  expect(byRevenueDesc[0].totalRevenue).toBe(1000);

  const byDateAsc = sortSales(sample, 'date', 'asc');
  expect(byDateAsc[0].saleDate).toBe('2023-01-01');
});
