import React, { useMemo } from 'react';

interface HeatmapProps {
  data: Array<{ date: string; count: number }>;
  year: number;
}

const getHeatClass = (count: number, max: number): string => {
  if (count === 0) return 'heat-0';
  const ratio = count / Math.max(max, 1);
  if (ratio < 0.2) return 'heat-1';
  if (ratio < 0.4) return 'heat-2';
  if (ratio < 0.6) return 'heat-3';
  if (ratio < 0.8) return 'heat-4';
  return 'heat-5';
};

const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
const DAYS   = ['','Mon','','Wed','','Fri',''];

export const Heatmap: React.FC<HeatmapProps> = ({ data, year }) => {
  const { weeks, maxCount } = useMemo(() => {
    const byDate: Record<string, number> = {};
    let max = 0;
    for (const d of data) {
      byDate[d.date] = d.count;
      if (d.count > max) max = d.count;
    }

    const start = new Date(`${year}-01-01`);
    // Pad to start on Sunday
    const startDow = start.getDay();
    const weeks: Array<Array<{ date: string; count: number } | null>> = [];
    let week: Array<{ date: string; count: number } | null> = Array(startDow).fill(null);

    const end = new Date(`${year}-12-31`);
    const cursor = new Date(start);
    while (cursor <= end) {
      const key = cursor.toISOString().slice(0, 10);
      week.push({ date: key, count: byDate[key] ?? 0 });
      if (week.length === 7) { weeks.push(week); week = []; }
      cursor.setDate(cursor.getDate() + 1);
    }
    if (week.length > 0) {
      while (week.length < 7) week.push(null);
      weeks.push(week);
    }
    return { weeks, maxCount: max };
  }, [data, year]);

  const monthLabels = useMemo(() => {
    const labels: Array<{ label: string; col: number }> = [];
    let lastMonth = -1;
    weeks.forEach((week, wi) => {
      const firstReal = week.find(d => d !== null);
      if (!firstReal) return;
      const m = new Date(firstReal.date).getMonth();
      if (m !== lastMonth) { labels.push({ label: MONTHS[m], col: wi }); lastMonth = m; }
    });
    return labels;
  }, [weeks]);

  return (
    <div className="overflow-x-auto">
      <div className="relative inline-flex flex-col gap-1 pt-6">
        {/* Month labels */}
        <div className="absolute top-0 left-7 flex" style={{ gap: '3px' }}>
          {monthLabels.map(({ label, col }) => (
            <span
              key={label}
              className="text-[10px] text-gray-500 absolute"
              style={{ left: col * 13 }}
            >
              {label}
            </span>
          ))}
        </div>

        <div className="flex gap-1">
          {/* Day-of-week labels */}
          <div className="flex flex-col" style={{ gap: '3px' }}>
            {DAYS.map((d, i) => (
              <span key={i} className="h-[10px] text-[10px] leading-[10px] text-gray-600 pr-1 w-6 text-right">
                {d}
              </span>
            ))}
          </div>

          {/* Grid */}
          {weeks.map((week, wi) => (
            <div key={wi} className="flex flex-col" style={{ gap: '3px' }}>
              {week.map((day, di) => (
                <div
                  key={di}
                  title={day ? `${day.date}: ${day.count} submissions` : ''}
                  className={`h-[10px] w-[10px] rounded-[2px] ${day ? getHeatClass(day.count, maxCount) : 'opacity-0'}`}
                />
              ))}
            </div>
          ))}
        </div>

        {/* Legend */}
        <div className="flex items-center gap-1.5 mt-2 justify-end">
          <span className="text-[10px] text-gray-500">Less</span>
          {[0,1,2,3,4,5].map(i => (
            <div key={i} className={`h-[10px] w-[10px] rounded-[2px] heat-${i}`} />
          ))}
          <span className="text-[10px] text-gray-500">More</span>
        </div>
      </div>
    </div>
  );
};
