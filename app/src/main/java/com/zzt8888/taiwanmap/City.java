package com.zzt8888.taiwanmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.ColorInt;

public class City {

    private Path path;
    private int color;
    private Paint paint;
    private boolean isSelected;


    public City(Path path, @ColorInt int color) {
        this.path = path;
        this.color = color;



    }

    public void onDraw(Canvas canvas) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(2);
        canvas.drawPath(path, paint);

        paint.setStrokeWidth(isSelected ? 5 : 2);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawPath(path, paint);

    }


    public void setPath(Path path) {
        this.path = path;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
