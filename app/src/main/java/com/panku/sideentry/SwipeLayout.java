package com.panku.sideentry;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by wangx on 2017/2/8.
 * 支持侧滑删除的自定义view   这里继承帧布局  ViewGroup的子类
 */
public class SwipeLayout extends FrameLayout {

    ViewDragHelper dragHelper;
    private ViewGroup backLayout;
    private ViewGroup frontLayout;
    private int mRange;
    private int mWidth;
    private int mHeight;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //1. 创建ViewDragHelper对象  这是2013年google i/o大会发布的  实现控件拖动的类
        //第一个参数表示:通过哪一个view管理当前拖动事件  这里设置this,当前view
        //第二个表示拖动敏感度 默认1.0f就可以了
        //第三个参数  是回调
        dragHelper = ViewDragHelper.create(this, 1.0f, callback);
    }

    //重写onInterceptTouchEvent()方法   将事件拦截交给ViewDragHelper这个拖动控件去处理
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    //重写onTouchEvent()方法   将触摸事件交给ViewDragHelper这个拖动控件去处理
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);//-----> 回调
        return true;//事件消费  move +up
    }

    //  xml ---渲染成>view的时候会调用
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (getChildCount() < 2) {
            throw new IllegalStateException("You must have 2 children at least!");
        }

        if (!(getChildAt(0) instanceof ViewGroup) || !(getChildAt(1) instanceof ViewGroup)) {
            throw new IllegalArgumentException("your children must be instanceof ViewGroup!");
        }

        backLayout = (ViewGroup) getChildAt(0);//覆盖在最上面的view   带有删除按钮的view
        frontLayout = (ViewGroup) getChildAt(1);//覆盖在下面的view    带有头像的view
    }

    // 测量子view 会调用多次
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /*测量后值发生改变后会调用的方法*/
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 当控件的宽高
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        // backlayout的宽 拖动范围
        mRange = backLayout.getMeasuredWidth();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        layoutInit(false);//设置view的位置
    }

    private void layoutInit(boolean isOpen) {
        Rect frontRect = computeFrontRect(isOpen);
        frontLayout.layout(frontRect.left, frontRect.top, frontRect.right, frontRect.bottom);
        Rect backRect = computeBackRect(frontRect);
        backLayout.layout(backRect.left, backRect.top, backRect.right, backRect.bottom);
    }

    /*根据前边的矩形计算后边矩形*/
    private Rect computeBackRect(Rect frontRect) {
        int left = frontRect.right;//前边的矩形距离右边的距离====后边矩形距离左边的距离
        return new Rect(left, frontRect.top, left + mRange, frontRect.bottom);
    }

    /**
     * 计算前边layout 矩形位置
     *
     * @param isOpen
     */
    private Rect computeFrontRect(boolean isOpen) {
        int left = 0;
        if (isOpen) {
            //如果是打开的  left左边移动的距离===右边矩形的宽度(负的mRange)
            left = -mRange;
        }
        //没有打开
        return new Rect(left, 0, left + getMeasuredWidth(), getMeasuredHeight());
    }

    // 3.处理回调
    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        /*返回值决定子view是否可以拖动  true可以拖动 */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            // child:当前swipelayout的直接子view
            //pointerId:多个手指操作
            return true;
        }

        /*拖动到哪个位置 还没有发生真正的移动  修正拖动范围*/
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            // child:当前拖动的子view
            // left:建议达到的位置 = 当前位置 + 瞬间变化量(向左 为-  向右为+)
            // dx: 瞬间变化量
//            System.out.println("left = " + left + "::" + frontLayout.getLeft() + "::dx = " + dx);
            // 修正前边的拖动范围
            if (child == frontLayout) {
                left = fixFrontLeft(left);
            } else if (child == backLayout) {
                left = fixBackLeft(left);
            }
            return left;
        }

        private int fixBackLeft(int left) {
            if (left < mWidth - mRange) {
                left = mWidth - mRange;
            } else if (left > mWidth) {
                left = mWidth;
            }
            return left;
        }

        private int fixFrontLeft(int left) {
            if (left < -mRange) {
                left = -mRange;
            } else if (left > 0) {
                left = 0;
            }
            return left;
        }

        /*获取横向拖动范围  不限制拖动范围 不决定是否拖动 抗干扰 >0即可*/
        @Override
        public int getViewHorizontalDragRange(View child) {
            return mRange;
        }

        /*已经发生了移动*/
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            //changedView: 正在拖动的view
            // left: clampViewPositionHorizontal的返回值
            //dx: 水平方向瞬间变化量
//            System.out.println("changedView>" + changedView + "::left= " + left + "::dx = " + dx);
            if (changedView == frontLayout) {
                // 瞬间变化量转交给backLayout
                backLayout.offsetLeftAndRight(dx);
            } else if (changedView == backLayout) {
                // 将瞬间变化量转交给frontLayout
                frontLayout.offsetLeftAndRight(dx);
            }

            //添加回调
            dispatchEvent();


            //收到重新绘制
            invalidate();//---->onDraw

        }


        /*释放的过程中调用*/
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            // releasedChild:释放的子view
            // xvel: s = vt  水平方向的速度  三种状态:1.向左滑动的时候值为负数  2.向右滑动值为正数    3.  值为0
            System.out.println("releasedChild = " + releasedChild + "::xvel = " + xvel);
            if (xvel == 0 && frontLayout.getLeft() < -mRange * 1.0f / 3) {  //-1 -2
                open();//打开侧滑
            } else if (xvel < 0) {
                open();//<0 说明是往左滑动了  打开它
            } else {
                close();//否则就是>0 说明向右滑动了  关闭它
            }
        }
    };

    public enum SwipeStatus {
        OPEN, CLOSE, SWIPING
    }

    public SwipeStatus status = SwipeStatus.CLOSE;//默认进来关闭状态

    /*添加回调*/
    private void dispatchEvent() {
        //记录上一个状态
        SwipeStatus preStatus = status;
        status = updateStatus();//获取当前侧滑打开状态
        if (preStatus != status) {
            if (onSwipeChangeListener != null) {
                if (status == SwipeStatus.OPEN) {
                    //打开
                    onSwipeChangeListener.onOpen(this);
                } else if (status == SwipeStatus.CLOSE) {
                    //关闭
                    onSwipeChangeListener.onClose(this);
                }


                //如果上一个状态是关闭
                if (preStatus == SwipeStatus.CLOSE) {
                    //将要打开
                    onSwipeChangeListener.onStartOpen(this);
                } else if (preStatus == SwipeStatus.OPEN) {
                    //如果上一个状态是打开  那就将要关闭
                    onSwipeChangeListener.onStartClose(this);
                }

            }
        }
    }

    //记录当前状态
    private SwipeStatus updateStatus() {
        if (frontLayout.getLeft() == -mRange) {
            return SwipeStatus.OPEN;
        } else if (frontLayout.getLeft() == 0) {
            return SwipeStatus.CLOSE;
        }
        return SwipeStatus.SWIPING;//正在侧滑
    }

    @Override
    public void computeScroll() { // 60+
        super.computeScroll();
        //是否达到目标值
        boolean b = dragHelper.continueSettling(true);
        if (b) {
            //动画方式平滑打开
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }


    public void close(boolean isSmooth) {
        if (isSmooth) {
            //是否到达了目标位置  ---> 触发 computeScroll方法
            boolean b = dragHelper.smoothSlideViewTo(frontLayout, 0, 0);
            if (b) {
                //动画方式打开
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutInit(false);
        }
    }

    /*关闭*/
    public void close() {
        close(true);//默认平滑关闭
    }


    public void open(boolean isSmooth) {
        if (isSmooth) {
            //是否到达了目标位置
            boolean b = dragHelper.smoothSlideViewTo(frontLayout, -mRange, 0);
            if (b) {
                //动画方式打开
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutInit(true);

        }
    }

    /*打开*/
    public void open() {
        open(true); //默认平滑打开
    }

    private OnSwipeChangeListener onSwipeChangeListener;

    public OnSwipeChangeListener getOnSwipeChangeListener() {
        return onSwipeChangeListener;
    }

    public void setOnSwipeChangeListener(OnSwipeChangeListener onSwipeChangeListener) {
        this.onSwipeChangeListener = onSwipeChangeListener;
    }

    public interface OnSwipeChangeListener {
        //打开
        void onOpen(SwipeLayout layout);

        void onClose(SwipeLayout layout);

        //将要打开
        void onStartOpen(SwipeLayout layout);

        //将要关闭
        void onStartClose(SwipeLayout layout);

    }
}
