package com.tochange.yang.sector.tools.screenshot;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tochange.yang.R;
import com.tochange.yang.lib.Utils;
import com.tochange.yang.lib.log;
import com.tochange.yang.sector.tools.screenshot.ScreenShotActivity.CaptureCallBack;

public class ScreenShotView extends View
{
    public static final int GROW_NONE = (1 << 0);// 框体外部

    public static final int GROW_LEFT_EDGE = (1 << 1);// 框体左边缘

    public static final int GROW_RIGHT_EDGE = (1 << 2);// 框体右边缘

    public static final int GROW_TOP_EDGE = (1 << 3);// 框体上边缘

    public static final int GROW_BOTTOM_EDGE = (1 << 4);// 框体下边缘

    public static final int GROW_MOVE = (1 << 5);// 框体移动

    private Paint outsideCapturePaint = new Paint(); // 捕获框体外部画笔

    private Paint lineCapturePaint = new Paint(); // 边框画笔

    private Rect viewRect; // 可视范围

    private Rect captureRect; // 框体范围

    private int mMotionEdge; // 触摸的边缘

    private float mLastX, mLastY; // 上次触摸的坐标

    private Drawable horStretchArrows; // 水平拉伸箭头

    private Drawable verStretchArrows; // 垂直拉伸箭头

    private int horStretchArrowsHalfWidth; // 水平拉伸箭头的宽

    private int horStretchArrowsHalfHeigth;// 水平拉伸箭头的高

    private int verStretchArrowsHalfWidth;// 垂直拉伸箭头的宽

    private int verStretchArrowsHalfHeigth;// 垂直拉伸箭头的高

    private ScreenShotView mCaptureView;

    private CaptureCallBack mCaptureCallBack;

    private enum ActionMode
    { // 枚举动作类型：无、移动、拉伸
        None, Move, Grow
    }

    private ActionMode mMode = ActionMode.None;

    /*
     * 以下变量用于自定义形状截图
     */
    private int l, t, r, b;

    private Path mPath = new Path();// 自定义图形

    private ArrayList<Point> mCustomShapePointList = new ArrayList<Point>();

    class Point
    {
        int x, y;

        public Point(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
    }

    /*
     * 以上变量用于自定义形状截图
     */

    public ScreenShotView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        lineCapturePaint.setStrokeWidth(2F); // 捕获框边框画笔大小
        lineCapturePaint.setStyle(Paint.Style.STROKE);// 画笔风格:空心
        lineCapturePaint.setAntiAlias(true); // 抗锯齿
        lineCapturePaint.setColor(0xaaccffff); // 画笔颜色#
        Resources resources = context.getResources();
        horStretchArrows = resources.getDrawable(R.drawable.hor_stretch_arrows);
        verStretchArrows = resources.getDrawable(R.drawable.ver_stretch_arrows);

        horStretchArrowsHalfWidth = horStretchArrows.getIntrinsicWidth() / 2;
        horStretchArrowsHalfHeigth = horStretchArrows.getIntrinsicHeight() / 2;
        verStretchArrowsHalfWidth = verStretchArrows.getIntrinsicWidth() / 2;
        verStretchArrowsHalfHeigth = verStretchArrows.getIntrinsicHeight() / 2;
        setFullScreen(true); // 默认为全屏模式
    }

    boolean isFirstDraw = true;

    int shape;

    int startX;

    int startY;

    public void setShape(int shape)
    {
        this.shape = shape;
        invalidate();
    }

    public void setCallBack(CaptureCallBack callback)
    {
        mCaptureCallBack = callback;
    }

    public void reset()
    {
        isFirstDraw = true;
        mPath.reset();
        mCustomShapePointList.clear();
        captureRect.bottom = captureRect.top = captureRect.right = captureRect.left = 0;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        l = left;
        t = top;
        r = right;
        b = bottom;
        // 初始化可视范围及框体大小
        viewRect = new Rect(left, top, right, bottom);
        captureRect = new Rect(0, 0, 0, 0);
        reset();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        canvas.save();

        if (shape == ScreenShotActivity.SHAPE_CUSTOM)
        {
            drawCustomCustomView(canvas);
            return;
        }
        Path path = new Path();
        if (shape == ScreenShotActivity.SHAPE_OVAL)
            path.addOval(new RectF(captureRect), Path.Direction.CW);// 顺时针闭合框体
        else if (shape == ScreenShotActivity.SHAPE_RECT)
            path.addRect(new RectF(captureRect), Path.Direction.CW);// 顺时针闭合框体
        // long press cann't change shot shap
        // if (mCaptureView != null && Build.VERSION.SDK_INT >
        // Build.VERSION_CODES.HONEYCOMB)
        // mCaptureView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        canvas.clipPath(path, Region.Op.DIFFERENCE);
        canvas.drawRect(viewRect, outsideCapturePaint); // 绘制框体外围区域

        canvas.drawPath(path, lineCapturePaint); // 绘制框体
        canvas.restore();
        if (mMode == ActionMode.Grow)
        { // 拉伸操作时，绘制框体箭头

            int xMiddle = captureRect.left + captureRect.width() / 2; // 框体中间X坐标
            int yMiddle = captureRect.top + captureRect.height() / 2; // 框体中间Y坐标

            // 框体左边的箭头
            horStretchArrows.setBounds(captureRect.left
                    - horStretchArrowsHalfWidth, yMiddle
                    - horStretchArrowsHalfHeigth, captureRect.left
                    + horStretchArrowsHalfWidth, yMiddle
                    + horStretchArrowsHalfHeigth);
            horStretchArrows.draw(canvas);

            // 框体右边的箭头
            horStretchArrows.setBounds(captureRect.right
                    - horStretchArrowsHalfWidth, yMiddle
                    - horStretchArrowsHalfHeigth, captureRect.right
                    + horStretchArrowsHalfWidth, yMiddle
                    + horStretchArrowsHalfHeigth);
            horStretchArrows.draw(canvas);

            // 框体上方的箭头
            verStretchArrows.setBounds(xMiddle - verStretchArrowsHalfWidth,
                    captureRect.top - verStretchArrowsHalfHeigth, xMiddle
                            + verStretchArrowsHalfWidth, captureRect.top
                            + verStretchArrowsHalfHeigth);
            verStretchArrows.draw(canvas);

            // 框体下方的箭头
            verStretchArrows.setBounds(xMiddle - verStretchArrowsHalfWidth,
                    captureRect.bottom - verStretchArrowsHalfHeigth, xMiddle
                            + verStretchArrowsHalfWidth, captureRect.bottom
                            + verStretchArrowsHalfHeigth);
            verStretchArrows.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if (captureRect.left == captureRect.right
                        && captureRect.top == captureRect.right
                        && captureRect.bottom == captureRect.top)
                {
                    startX = (int) event.getRawX();
                    startY = (int) event.getY();
                }

                int grow = getGrow(event.getX(), event.getY());
                if (grow != GROW_NONE)
                {
                    // 锁定当前触摸事件的操作对象，直到ACTION_UP，如果没有锁定，有grow为前次操作的值。
                    mCaptureView = ScreenShotView.this;
                    mMotionEdge = grow;
                    mLastX = event.getX();
                    mLastY = event.getY();
                    mCaptureView.setMode((grow == GROW_MOVE) ? ActionMode.Move
                            : ActionMode.Grow);
                }
                if (addCustomPoint(x, y))
                    break;
                break;
            case MotionEvent.ACTION_UP:
                if (addCustomPoint(x, y))
                    break;

                isFirstDraw = false;
                if (mCaptureView != null)
                {
                    setMode(ActionMode.None);
                    mCaptureView = null; // 释放当前锁定的操作对象
                }
                if (mCaptureCallBack != null)
                    mCaptureCallBack.upActionNotify();
                break;
            case MotionEvent.ACTION_MOVE: // 框体移动
                if (isFirstDraw)
                {
                    if (y > startY)
                    {
                        captureRect.bottom = y;
                        captureRect.top = startY;
                    }
                    else
                    {
                        captureRect.bottom = startY;
                        captureRect.top = y;
                    }
                    if (x > startX)
                    {
                        captureRect.right = x;
                        captureRect.left = startX;
                    }
                    else
                    {
                        captureRect.right = startX;
                        captureRect.left = x;
                    }
                    invalidate();
                }
                if (mCaptureView != null)
                {
                    handleMotion(mMotionEdge, event.getX() - mLastX,
                            event.getY() - mLastY);
                    mLastX = event.getX();
                    mLastY = event.getY();
                }
                break;
        }
        return true;
    }

    private boolean addCustomPoint(int x, int y)
    {
        if (shape == ScreenShotActivity.SHAPE_CUSTOM)
        {
            if (!mCustomShapePointList.isEmpty())
            {
                Point last = mCustomShapePointList.get(mCustomShapePointList
                        .size() - 1);
                int distant = (int) Math.sqrt((last.x - x) * (last.x - x)
                        + (last.y - y) * (last.y - y));
                if (distant < 20)
                    return true;
            }
            mPath.reset();
            mCustomShapePointList.add(new Point(x, y));
            if (mCustomShapePointList.size() >= 2)
            {
                mPath.moveTo(mCustomShapePointList.get(0).x,
                        mCustomShapePointList.get(0).y);
                for (int i = 1; i < mCustomShapePointList.size(); i++)
                {
                    mPath.lineTo(mCustomShapePointList.get(i).x,
                            mCustomShapePointList.get(i).y);
                }
                mPath.close();
            }
            invalidate();
            if (mCaptureCallBack != null && mCustomShapePointList.size() >= 3)
                mCaptureCallBack.upActionNotify();
            return true;
        }
        return false;
    }

    public void setFullScreen(boolean full)
    {
        // 全屏，则把外部框体颜色设为透明
        if (full)
            outsideCapturePaint.setARGB(100, 200, 200, 200);
        // 只显示框体区域，框体外部为全黑
        else
            outsideCapturePaint.setARGB(255, 0, 0, 0);
    }

    private void setMode(ActionMode mode)
    {
        if (mode != mMode)
        {
            mMode = mode;
            invalidate();
        }
    }

    // 确定触摸位置及动作，分别为触摸框体外围和框体上、下、左、右边缘以及框体内部。
    private int getGrow(float x, float y)
    {
        final float effectiveRange = 20F; // 触摸的有效范围大小
        int grow = GROW_NONE;
        int left = captureRect.left;
        int top = captureRect.top;
        int right = captureRect.right;
        int bottom = captureRect.bottom;
        boolean verticalCheck = (y >= top - effectiveRange)
                && (y < bottom + effectiveRange);
        boolean horizCheck = (x >= left - effectiveRange)
                && (x < right + effectiveRange);

        // 触摸了框体左边缘
        if ((Math.abs(left - x) < effectiveRange) && verticalCheck)
            grow |= GROW_LEFT_EDGE;

        // 触摸了框体右边缘
        if ((Math.abs(right - x) < effectiveRange) && verticalCheck)
            grow |= GROW_RIGHT_EDGE;

        // 触摸了框体上边缘
        if ((Math.abs(top - y) < effectiveRange) && horizCheck)
            grow |= GROW_TOP_EDGE;

        // 触摸了框体下边缘
        if ((Math.abs(bottom - y) < effectiveRange) && horizCheck)
            grow |= GROW_BOTTOM_EDGE;

        // 触摸框体内部
        if (grow == GROW_NONE && captureRect.contains((int) x, (int) y))
            grow = GROW_MOVE;
        return grow;
    }

    // 处理触摸事件，判断移动框体还是伸缩框体
    private void handleMotion(int grow, float dx, float dy)
    {
        if (grow == GROW_NONE)
            return;
        else if (grow == GROW_MOVE)
            moveBy(dx, dy); // 移动框体
        else
        {
            if (((GROW_LEFT_EDGE | GROW_RIGHT_EDGE) & grow) == 0)
                dx = 0; // 水平不伸缩
            if (((GROW_TOP_EDGE | GROW_BOTTOM_EDGE) & grow) == 0)
                dy = 0; // 垂直不伸缩

            growBy(grow, (((grow & GROW_LEFT_EDGE) != 0) ? -1 : 1) * dx,
                    (((grow & GROW_TOP_EDGE) != 0) ? -1 : 1) * dy);
        }
    }

    private void moveBy(float dx, float dy)
    {
        Rect invalRect = new Rect(captureRect);
        captureRect.offset((int) dx, (int) dy);
        captureRect.offset(Math.max(0, viewRect.left - captureRect.left),
                Math.max(0, viewRect.top - captureRect.top));
        captureRect.offset(Math.min(0, viewRect.right - captureRect.right),
                Math.min(0, viewRect.bottom - captureRect.bottom));

        // 清除移动滞留的痕迹
        invalRect.union(captureRect);// 更新围绕本身区域和指定的区域，
        invalRect.inset(-100, -100);
        invalidate(invalRect); // 重绘指定区域
    }

    private void growBy(int grow, float dx, float dy)
    {
        float widthCap = 50F; // captureRect最小宽度
        float heightCap = 50F; // captureRect最小高度

        RectF r = new RectF(captureRect);

        // 当captureRect拉伸到宽度 = viewRect的宽度时，则调整dx的值为 0
        if (dx > 0F && r.width() + 2 * dx >= viewRect.width())
            dx = 0F;

        // 同上
        if (dy > 0F && r.height() + 2 * dy >= viewRect.height())
            dy = 0F;

        if (((grow & GROW_TOP_EDGE) != 0) && (dx == 0))
            r.set(captureRect.left, captureRect.top - dy, captureRect.right,
                    captureRect.bottom);
        else if (((grow & GROW_BOTTOM_EDGE) != 0) && (dx == 0))
            r.set(captureRect.left, captureRect.top, captureRect.right,
                    captureRect.bottom + dy);
        else if (((grow & GROW_LEFT_EDGE) != 0) && (dy == 0))
            r.set(captureRect.left - dx, captureRect.top, captureRect.right,
                    captureRect.bottom);
        else if (((grow & GROW_RIGHT_EDGE) != 0) && (dy == 0))
            r.set(captureRect.left, captureRect.top, captureRect.right + dx,
                    captureRect.bottom);
        else
            r.inset(-dx, -dy); // 框体边缘外移,原来只有这一句。yangxj@20140526

        // 当captureRect缩小到宽度 = widthCap时
        if (r.width() <= widthCap)
            r.inset(-(widthCap - r.width()) / 2F, 0F);
        // 同上
        if (r.height() <= heightCap)
            r.inset(0F, -(heightCap - r.height()) / 2F);

        if (r.left < viewRect.left)
            r.offset(viewRect.left - r.left, 0F);
        else if (r.right > viewRect.right)
            r.offset(-(r.right - viewRect.right), 0);

        if (r.top < viewRect.top)
            r.offset(0F, viewRect.top - r.top);
        else if (r.bottom > viewRect.bottom)
            r.offset(0F, -(r.bottom - viewRect.bottom));

        captureRect.set((int) r.left, (int) r.top, (int) r.right,
                (int) r.bottom);
        invalidate();
    }

    public synchronized Path getCustomPath(int statusBarHeight)

    {
        ArrayList<Point> list = new ArrayList<Point>();
        Path p = new Path();
        list.addAll(mCustomShapePointList);
        for (Point pp : list)
            pp.y += statusBarHeight;
        if (list.size() >= 3)
        {
            p.moveTo(list.get(0).x, list.get(0).y);
            for (int i = 1; i < list.size(); i++)
                p.lineTo(list.get(i).x, list.get(i).y);
            p.close();
            return p;
        }
        else
            return null;
    }

    public synchronized Rect getCaptureRect(int statusBarHight)
    {
        captureRect.top += statusBarHight;
        captureRect.bottom += statusBarHight;
        return captureRect;
    }

    private void clear(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
        // paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
    }

    private void drawCustomCustomView(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.reset();
        paint.setARGB(100, 200, 200, 200);
        canvas.clipPath(mPath, Region.Op.DIFFERENCE);
        canvas.drawRect(new Rect(l, t, r, b), paint); // 绘制框体外围区域

        paint.reset();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xaaccffff);
        paint.setAntiAlias(true);// 设置画笔的锯齿效果
        canvas.drawPath(mPath, paint);

        paint.setColor(0xffccffff);
        paint.setStrokeWidth(2);
        for (int i = 0; i < mCustomShapePointList.size(); i++)
            canvas.drawCircle(mCustomShapePointList.get(i).x,
                    mCustomShapePointList.get(i).y, 5, paint);// end point

    }

}
