package yqb.com.example.weatherdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

public class MyGridView extends GridView{
	public MyGridView(Context context) {
        super(context);
    }

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        int column = getNumColumns();
        int row = (int) Math.ceil(getChildCount() / (float)column);
        int childCount = getChildCount();
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.WHITE);
        for (int i = 0; i<childCount ; i++){
            View child = getChildAt(i);
            if((i+1) > column * (row - 1)){
                if((i + 1) == column * row){
                    continue;
                }else{
                    canvas.drawLine(child.getRight(),child.getTop(),child.getRight(),child.getBottom(),linePaint);
                }
            }else if ((i+1) % column ==0){
                canvas.drawLine(child.getLeft(),child.getBottom(),child.getRight(),child.getBottom(),linePaint);
            }else{
                canvas.drawLine(child.getLeft(),child.getBottom(),child.getRight(),child.getBottom(),linePaint);
                canvas.drawLine(child.getRight(),child.getTop(),child.getRight(),child.getBottom(),linePaint);
            }
        }
    }
}
