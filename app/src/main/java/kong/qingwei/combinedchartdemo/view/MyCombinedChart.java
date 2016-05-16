package kong.qingwei.combinedchartdemo.view;

import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.listener.OnDrawListener;

import java.util.ArrayList;

import kong.qingwei.combinedchartdemo.utils.MyChartHighlighter;
import kong.qingwei.combinedchartdemo.utils.MyCustomXAxisValueFormatter;
import kong.qingwei.combinedchartdemo.bean.CombinedChartEntity;

/**
 * Created by kqw on 2016/5/16.
 * MyCombinedChart
 */
public class MyCombinedChart extends CombinedChart implements OnChartGestureListener, OnChartValueSelectedListener {

    private static final String TAG = "MyCombinedChart";

    private boolean isTranslate;
    private final int mWidth;
    private final Vibrator mVibrator;
    private MyChartHighlighter myChartHighlighter;

    public MyCombinedChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initChart();

        mVibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);

        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mWidth = windowManager.getDefaultDisplay().getWidth();
    }

    private void initChart() {
        setDescription("");
        setBackgroundColor(Color.WHITE);
        setDrawGridBackground(false);
        setDrawBarShadow(false);

        // 取消Y轴缩放动画
        setScaleYEnabled(false);

        // 自动缩放调整
        setAutoScaleMinMaxEnabled(true);

//        YAxis leftAxis = getAxisLeft();
//        leftAxis.setDrawGridLines(false);
//        YAxis rightAxis = getAxisRight();
//        rightAxis.setDrawGridLines(true);

        YAxis left = getAxisLeft();
        // 左侧Y轴坐标
        left.setDrawLabels(true);
        // 左侧Y轴
        left.setDrawAxisLine(true);
        // 横向线
        left.setDrawGridLines(true);
        left.setDrawZeroLine(true);

        left.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        getAxisRight().setEnabled(false);

        /*
        * X轴
        * ******************************************************************************/
        XAxis xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        // 格式化X轴时间
        xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());


        /*
        * 图形触摸监听
        * *********************************************************************************/
        setOnChartGestureListener(this);

        /*
        * 被选择监听（高亮监听）
        * **********************************************************************************/
        setOnChartValueSelectedListener(this);

        // 用来将像素转成index
        myChartHighlighter = new MyChartHighlighter(this);

    }

    /**
     * 填充数据
     *
     * @param entity 数据实体
     */
    public void setData(CombinedChartEntity entity) {
        ArrayList<String> timeY = new ArrayList<>();
        for (int i = 0; i < entity.getData().size(); i++) {
            timeY.add(entity.getData().get(i).get(0) + "");
        }
        CombinedData data = new CombinedData(timeY);
        data.setData(generateCandleData(entity));
        data.setData(generateLineData(entity));
        //        data.setData(generateBarData(empty));
        //        data.setData(generateBubbleData());
        //        data.setData(generateScatterData());
        setData(data);

        notifyDataSetChanged();

        // 最多显示60组数据
        setVisibleXRangeMaximum(60);
        // 最少显示30组数据
        setVisibleXRangeMinimum(30);
        // 显示
        invalidate();
        // 移动到最右侧数据
        moveViewToX(entity.getData().size() - 1);

    }

    protected CandleData generateCandleData(CombinedChartEntity entity) {
        CandleData d = new CandleData();
        ArrayList<CandleEntry> entries = new ArrayList<>();
        for (int index = 0; index < entity.getData().size(); index++) {
            long a = entity.getData().get(index).get(1) / 1000;
            long b = entity.getData().get(index).get(2) / 1000;
            long c = entity.getData().get(index).get(3) / 1000;
            long dd = entity.getData().get(index).get(4) / 1000;
            entries.add(new CandleEntry(index, a, b, c, dd));
        }
        CandleDataSet set = new CandleDataSet(entries, "K线");
        set.setColor(Color.rgb(80, 80, 80));
        set.setValueTextSize(10f);
        set.setDrawValues(false);
        d.addDataSet(set);
        return d;
    }

    private LineData generateLineData(CombinedChartEntity entity) {
        LineData d = new LineData();
        d.addDataSet(getLineDataSet(5, entity));
        d.addDataSet(getLineDataSet(10, entity));
        d.addDataSet(getLineDataSet(30, entity));
        return d;
    }

    private LineDataSet getLineDataSet(int ma, CombinedChartEntity empty) {

        ArrayList<Entry> entries = new ArrayList<>();
        for (int index = ma - 1; index < empty.getData().size(); index++) {
            long sum = 0;
            for (int m = 0; m < ma; m++) {
                sum += (empty.getData().get(index - m).get(3) / 1000);
            }
            sum /= ma;
            entries.add(new Entry(sum, index));
        }
        LineDataSet set = new LineDataSet(entries, "MA " + ma);
        set.setColor(5 == ma ? Color.rgb(240, 0, 70) : 10 == ma ? Color.rgb(0, 0, 70) : Color.rgb(100, 100, 255));
        set.setLineWidth(1f);
        set.setDrawCircles(false);
        set.setDrawCubic(false);
        set.setDrawValues(false);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.rgb(240, 238, 70));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }


    /*
    * Gesture callbacks
    * Start
    * *******************************************************************************/
    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
//        Log.i(TAG, "onChartGestureStart");
        isTranslate = false;
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
//        Log.i(TAG, "onChartGestureEnd");
        setDragEnabled(true);
        getData().setHighlightEnabled(false);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
//        Log.i(TAG, "onChartLongPressed");
        if (!isTranslate) {
            Toast.makeText(getContext().getApplicationContext(), "长按\n震动50毫秒\n可以左右滑动  查看数据", Toast.LENGTH_SHORT).show();
            // 震动50毫秒
            mVibrator.vibrate(50);
            setDragEnabled(false);
            getData().setHighlightEnabled(true);

            float x = me.getRawX();
            // TODO 通过像素换算index  高亮显示
            int index = myChartHighlighter.getXIndex(x);
            highlightValue(index, 0);
        }
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
//        Log.i(TAG, "onChartDoubleTapped");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
//        Log.i(TAG, "onChartSingleTapped");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
//        Log.i(TAG, "onChartFling");
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
//        Log.i(TAG, "onChartScale");
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        isTranslate = true;
    }
    /* End *******************************************************************************/

    /*
    * Selection callbacks
    * Start
    * *******************************************************************************/
    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        // TODO index转换成像素
//        float f = me.getRawX();
//        if (f < mWidth / 2) {
//            Log.i(TAG, "显示在右侧");
//        } else {
//            Log.i(TAG, "显示在左侧");
//        }
    }

    @Override
    public void onNothingSelected() {
        Log.i(TAG, "onNothingSelected");
    }
    /* End *******************************************************************************/
}