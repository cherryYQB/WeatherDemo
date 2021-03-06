package yqb.com.example.weatherdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import yqb.com.example.weatherdemo.utils.BaseUtils;

public class WeatherChart extends View {
	private static final int DAY_NUM = 3;
    private int[] temMax = new int[DAY_NUM];
    private int[] temMin = new int[DAY_NUM];
    private Bitmap[] weatherIcon = new Bitmap[DAY_NUM];
    private String[] time = new String[DAY_NUM];
    private int mHeight;
    private int mWidth;
    private Paint mTextPaint;
    private static final float TEXT_SIZE  = 20;
    private static final int TEXT_COLOR  = Color.WHITE;
    private static final int PART_MARGIN = 30;
    private int maxInMax;
    private int minInMax;
    private int maxInMin;
    private int minInMin;
    private int diffInMax;
    private int diffInMin;
    private int partHeight;
    private int dayWidth;
    private int firstPointX;
    private int diffInMaxHeight;
    private int diffInMinHeight;
    private static final int PART = 3;
    private static final int DIVIDE_MARGIN = 10;
    private Paint mDayDividePaint;
    private static final int DIVIDE_COLOR = Color.parseColor("#42CDFF");
    private static final int DIVIDE_WIDTH = 2;
    private Paint mPointPaint;
    private static final int POINT_COLOR = Color.WHITE;
    private static final int POINT_RADIUS = 5;
    private Paint mMaxLinePaint;
    private static final int MAX_LINE_COLOR = Color.WHITE;
    private static final int TEMPERATURE_LINE_WIDTH = 2;
    private Paint mMinLintPaint;
    private static final int MIN_LINE_COLOR = Color.WHITE;
    private static final String DEGREE = BaseUtils.getContext().getString(R.string.degree);
    private static final int TEXT_MARGIN = 7;
	
	public WeatherChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public WeatherChart(Context context) {
		this(context,null);
	}
	
	private void init() {
        initPaint();
    }
	
	private void initPaint(){
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(TEXT_SIZE);
        mTextPaint.setColor(TEXT_COLOR);

        mDayDividePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDayDividePaint.setStrokeWidth(DIVIDE_WIDTH);
        mDayDividePaint.setColor(DIVIDE_COLOR);

        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setColor(POINT_COLOR);
        mPointPaint.setStyle(Paint.Style.FILL);

        mMaxLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMaxLinePaint.setColor(MAX_LINE_COLOR);
        mMaxLinePaint.setStyle(Paint.Style.STROKE);
        mMaxLinePaint.setStrokeWidth(TEMPERATURE_LINE_WIDTH);

        mMinLintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinLintPaint.setColor(MIN_LINE_COLOR);
        mMinLintPaint.setStyle(Paint.Style.STROKE);
        mMinLintPaint.setStrokeWidth(TEMPERATURE_LINE_WIDTH);
    }
	
	private void computer() {
        maxInMax = getMax(temMax);
        minInMax = getMin(temMax);

        maxInMin = getMax(temMin);
        minInMin = getMin(temMin);

        diffInMax = maxInMax - minInMax;
        diffInMin = maxInMin - minInMin;

        diffInMaxHeight = diffInMax == 0 ? 0: (partHeight- PART_MARGIN ) / diffInMax;
        diffInMinHeight = diffInMin == 0 ? 0: (partHeight- PART_MARGIN ) / diffInMin;

        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < DAY_NUM ; i++){
            if(i==0){
                time[i] = getResources().getString(R.string.today);
                continue;
            }
            calendar.add(Calendar.DAY_OF_WEEK,1);
            time[i] = new SimpleDateFormat("EEEE",getResources().getConfiguration().locale)
                    .format(calendar.getTime())
                    .replace(getResources().getString(R.string.xingqi), getResources().getString(R.string.week));
        }
    }

    private int getMin(int[] m) {
        int tmp = m[0];
        for (int i : m) {
            tmp = tmp < i ? tmp : i;
        }
        return tmp;
    }

    private int getMax(int[] m) {
        int tmp = m[0];
        for (int i : m) {
            tmp = tmp > i ? tmp : i;
        }
        return tmp;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        mWidth = w;
        partHeight = mHeight / PART;
        dayWidth = mWidth / DAY_NUM;
        firstPointX = dayWidth / 2;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(getResources().getColor(R.color.colorTransparent));
        computer();
        drawDayDivide(canvas);
        drawPointAndTemperatureLine(canvas);
        drawWeatherIconAndText(canvas);
    }

    private void drawWeatherIconAndText(Canvas canvas) {
        Bitmap icon = null;
        for (int i = 0 ; i < DAY_NUM ; i++){
            icon = weatherIcon[i];
            if(icon==null){
            	continue;
            }
            icon = scaleBitmap(icon, 0.666f);
            canvas.drawBitmap(icon,dayWidth / 2 - icon.getWidth()/2 + dayWidth * i,partHeight * 2 +10, null);
            canvas.drawText(time[i], dayWidth / 2 - mTextPaint.measureText(time[i])/2 +dayWidth*i, partHeight * 2 + icon.getHeight() + partHeight * 2 / 5,mTextPaint);
        }
    }
    
    private Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        return newBM;
    }
    
    private void drawPointAndTemperatureLine(Canvas canvas) {
        int x,y;
        Path maxPath = new Path();
        Path minPath = new Path();
        String temp ;
        for(int i = 0 ; i < DAY_NUM ; i++){
            x = firstPointX + dayWidth * i;

            if(diffInMax == 0){
                y = partHeight / 2;
            }else{
                y = Math.abs(temMax[i] - maxInMax) * diffInMaxHeight + PART_MARGIN;
            }
            canvas.drawCircle(x , y , POINT_RADIUS , mPointPaint);
            temp = temMax[i]+DEGREE;
            canvas.drawText(temp , x - mTextPaint.measureText(temp)/2 , y - TEXT_MARGIN , mTextPaint);
            if(i == 0){
                maxPath.moveTo(x , y);
            }else{
                maxPath.lineTo(x , y);
            }

            if(diffInMin == 0){
                y = partHeight / 2 + partHeight;
            }else{
                y = Math.abs(temMin[i] - maxInMin) * diffInMinHeight + PART_MARGIN + partHeight;
            }
            canvas.drawCircle(x , y , POINT_RADIUS , mPointPaint);
            temp = temMin[i]+DEGREE;
            canvas.drawText(temp , x - mTextPaint.measureText(temp)/2 , y - TEXT_MARGIN , mTextPaint);
            if(i == 0){
                minPath.moveTo(x , y);
            }else{
                minPath.lineTo(x , y);
            }
        }
        canvas.drawPath(maxPath , mMaxLinePaint);
        canvas.drawPath(minPath , mMinLintPaint);
    }

    private void drawDayDivide(Canvas canvas) {
        for(int i = 1 ; i < DAY_NUM+1 ; i++){
            canvas.drawLine(i * dayWidth, DIVIDE_MARGIN, i * dayWidth, mHeight - DIVIDE_MARGIN, mDayDividePaint);
        }
    }

    public void setTemperatureAndIcon(int[] max,int min[],int icon[]){
        temMax = max;
        temMin = min;
        for (int i = 0; i < DAY_NUM; i++) {
			weatherIcon[i] = BitmapFactory.decodeResource(getResources(), icon[i]);
		}
        invalidate();
    }
}
