package com.zzt8888.taiwanmap;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class City {

    private Path path;
    private int color;
    private Paint paint;
    private boolean isSelected;
    private float fraction;
    private View view;

    private String name;

    private PathMeasure pathMeasure;
    private ValueAnimator animator;

    public City(String name, Path path, @ColorInt int color, View view) {
        this.path = path;
        this.color = color;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.view = view;
        this.name = name;
        pathMeasure = new PathMeasure(path, false);

    }

    public void onDraw(Canvas canvas) {

        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(2);

        canvas.drawPath(path, paint);

        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);

        if (isSelected) {
            paint.setStrokeWidth(5);
            paint.setColor(Color.WHITE);
            canvas.drawPath(path, paint);

            paint.setColor(Color.RED);
            Path dst = new Path();

            float pathLength = pathMeasure.getLength();
            float cursorLength = 20;
            cursorLength = Math.max(cursorLength, pathLength / 20);

            pathMeasure.getSegment(fraction * pathLength, fraction * pathLength + cursorLength, dst, true);
            //保证path中不止一条闭合曲线的情况下，动画依然可以进行
            if (fraction * pathLength + cursorLength >= pathLength)
                //当动画跑完整个轮廓，移动到下一个轮廓
                if (!pathMeasure.nextContour()) {
                    pathMeasure = new PathMeasure(path, false);
                }
            canvas.drawPath(dst, paint);
        }

    }


    public void animationPath(boolean isSelected) {

        if (isSelected) {
            animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(5000);
            animator.setInterpolator(new LinearInterpolator());
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    fraction = animation.getAnimatedFraction();
                    view.invalidate();

                }
            });
            animator.start();
        } else {
            if (animator != null)
                animator.cancel();
        }
    }


    public boolean isContainXY(float x, float y) {
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        Region region = new Region();
        region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        return region.contains((int) x, (int) y);
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getName() {
        return name;
    }
}
