export const filterSales = (sales = [], { variant = '', size = '' } = {}) => {
  let result = sales;
  if (variant) result = result.filter(s => s.variant === variant);
  if (size) result = result.filter(s => s.size === size);
  return result;
};

export const sortSales = (sales = [], type = 'date', order = 'asc') => {
  const sorted = [...sales];
  if (type === 'quantity') {
    sorted.sort((a, b) => (order === 'asc' ? a.quantity - b.quantity : b.quantity - a.quantity));
  } else if (type === 'revenue') {
    sorted.sort((a, b) => (order === 'asc' ? a.totalRevenue - b.totalRevenue : b.totalRevenue - a.totalRevenue));
  } else if (type === 'date') {
    sorted.sort((a, b) => {
      const dateA = new Date(a.saleDate);
      const dateB = new Date(b.saleDate);
      return order === 'asc' ? dateA - dateB : dateB - dateA;
    });
  }
  return sorted;
};

export default { filterSales, sortSales }; // eslint-disable-line import/no-anonymous-default-export
