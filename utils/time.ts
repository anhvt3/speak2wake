export function formatTime(hour: number, minute: number): string {
  return `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
}

export function getCountdownText(targetHour: number, targetMinute: number): string {
  const now = new Date();
  const target = new Date();
  target.setHours(targetHour, targetMinute, 0, 0);

  if (target <= now) {
    target.setDate(target.getDate() + 1);
  }

  const diffMs = target.getTime() - now.getTime();
  const hours = Math.floor(diffMs / (1000 * 60 * 60));
  const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));

  if (hours === 0) return `${minutes}m`;
  return `${hours}h ${minutes}m`;
}

export function getRepeatDaysText(days: number[]): string {
  if (days.length === 0) return 'Once';
  if (days.length === 7) return 'Every day';

  const weekdays = [1, 2, 3, 4, 5];
  const weekend = [0, 6];

  if (weekdays.every(d => days.includes(d)) && days.length === 5) return 'Weekdays';
  if (weekend.every(d => days.includes(d)) && days.length === 2) return 'Weekends';

  const labels = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  return days.map(d => labels[d]).join(', ');
}
