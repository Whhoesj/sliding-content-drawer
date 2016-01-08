package com.wouterhabets.slidingcontentdrawer.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.wouterhabets.slidingcontentdrawer.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * DrawerLayout acts aas a top-level container for window content that allows for a drawer to be
 * revealed by dragging the content.
 * <p>When dragging from the edge, the content will move to the right and scale down.
 * The menu, which sits below the content will be revealed with a scale and alpha effect.</p>
 * <p/>
 * <p>Drawer positioning and layout is controlled using the <code>android:layout_gravity</code>
 * attribute on child views corresponding to which side of the view you want the drawer
 * to emerge from: left or right. (Or start/end on platform versions that support layout direction.)
 * </p>
 * <p/>
 * <p>To use a DrawerLayout, place a {@link FrameLayout} as first child and a
 * {@link CoordinatorLayout} as second child. The width and height of both should be set to
 * <code>match_parent</code>. Use the FrameLayout for the menu and the CoordinatorLayout for the
 * AppBar/Toolbar and the content.</p>
 * <p/>
 * <p>{@link android.support.v4.widget.DrawerLayout.DrawerListener} can be used to monitor the state
 * and motion of drawer views.Avoid performing expensive operations such as layout during animation
 * as it can cause stuttering; try to perform expensive operations during the {@link #STATE_IDLE}
 * state. {@link android.support.v4.widget.DrawerLayout.SimpleDrawerListener} offers default/no-op
 * implementations of each callback method.</p>
 * <p/>
 * <p>As per the <a href="{@docRoot}design/patterns/navigation-drawer.html">Android Design
 * guide</a>, any drawers positioned to the left/start should always contain content for navigating
 * around the application</p>
 * <p/>
 * <p>Created by Wouter Habets (wouterhabets@gmail.com) on 5-1-16.</p>
 */
public class SlidingDrawerLayout extends FrameLayout {

    /**
     * Indicates that any drawers are in an idle, settled state. No animation is in progress.
     */
    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;
    /**
     * Indicates that a drawer is currently being dragged by the user.
     */
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;
    /**
     * Indicates that a drawer is in the process of settling to a final position.
     */
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;
    /**
     * The drawer is unlocked.
     */
    public static final int LOCK_MODE_UNLOCKED = 0;
    /**
     * The drawer is locked closed. The user may not open it, though
     * the app may open it programmatically.
     */
    public static final int LOCK_MODE_LOCKED_CLOSED = 1;
    /**
     * The drawer is locked open. The user may not close it, though the app
     * may close it programmatically.
     */
    public static final int LOCK_MODE_LOCKED_OPEN = 2;

    private static final String TAG_MENU = "menu";
    private static final String TAG_CONTENT = "content";
    private static final float CONTENT_SCALE_CLOSED = 1.0f;
    private static final float CONTENT_SCALE_OPEN = 0.7f;
    private static final float MENU_SCALE_CLOSED = 1.1f;
    private static final float MENU_SCALE_OPEN = 1.0f;
    private static final float MENU_ALPHA_CLOSED = 0.0f;
    private static final float MENU_ALPHA_OPEN = 1.0f;
    private static final float MARGIN_FACTOR = 0.7f;

    private float mContentScaleClosed = CONTENT_SCALE_CLOSED;
    private float mContentScaleOpen = CONTENT_SCALE_OPEN;
    private float mMenuScaleClosed = MENU_SCALE_CLOSED;
    private float mMenuScaleOpen = MENU_SCALE_OPEN;
    private float mMenuAlphaClosed = MENU_ALPHA_CLOSED;
    private float mMenuAlphaOpen = MENU_ALPHA_OPEN;
    private float mMarginFactor = MARGIN_FACTOR;

    private final ViewDragHelper mViewDragHelper;
    private ViewDragCallback mViewDragCallback;
    private View mContent;
    private View mMenu;
    private float mDragOffset;
    @LockMode
    private int mLockMode;
    private android.support.v4.widget.DrawerLayout.DrawerListener mDrawerListener;
    @State
    private int mDrawerState = STATE_IDLE;

    public SlidingDrawerLayout(Context context) {
        this(context, null);
    }

    public SlidingDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SlidingDrawerLayout, 0, 0);
        try {
            mContentScaleClosed = a.getFloat(R.styleable.SlidingDrawerLayout_contentScaleClosed, CONTENT_SCALE_CLOSED);
            mContentScaleOpen = a.getFloat(R.styleable.SlidingDrawerLayout_contentScaleOpen, CONTENT_SCALE_OPEN);
            mMenuScaleClosed = a.getFloat(R.styleable.SlidingDrawerLayout_menuScaleClosed, MENU_SCALE_CLOSED);
            mMenuScaleOpen = a.getFloat(R.styleable.SlidingDrawerLayout_menuScaleOpen, MENU_SCALE_OPEN);
            mMenuAlphaClosed = a.getFloat(R.styleable.SlidingDrawerLayout_menuAlphaClosed, MENU_ALPHA_CLOSED);
            mMenuAlphaOpen = a.getFloat(R.styleable.SlidingDrawerLayout_menuAlphaOpen, MENU_ALPHA_OPEN);
            mMarginFactor = a.getFloat(R.styleable.SlidingDrawerLayout_marginFactor, MARGIN_FACTOR);
        } finally {
            a.recycle();
        }
        Log.d(this.getClass().getSimpleName(), "MF: " + mMarginFactor);
        mViewDragCallback = new ViewDragCallback();
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mViewDragCallback);
    }

    private float map(float x, float inMin, float inMax, float outMin, float outMax) {
        return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            try {
                String tag = (String) view.getTag();
                if (tag.equals(TAG_CONTENT)) {
                    mContent = view;
                } else if (tag.equals(TAG_MENU)) {
                    mMenu = view;
                }
            } catch (Exception ignored) {
            }
            if (mContent != null && mMenu != null) break;
        }

        if (mContent == null) {
            throw new IllegalStateException("Missing content layout. " +
                    "Set a \"content\" tag on the content layout (in XML android:xml=\"content\")");
        } else if (mMenu == null) {
            throw new IllegalStateException("Missing menu layout." +
                    "Set a \"menu\" tag on the menu layout (in XML android:xml=\"menu\")");
        }

        closeDrawer();
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * Kept for compatibility. {@see #isDrawerOpen()}.
     *
     * @param gravity Ignored
     * @return true if the drawer view in in an open state
     */
    @SuppressWarnings("UnusedParameters")
    public boolean isDrawerOpen(int gravity) {
        return isDrawerOpen();
    }

    /**
     * Check if the drawer view is currently in an open state.
     * To be considered "open" the drawer must have settled into its fully
     * visible state.
     *
     * @return true if the drawer view is in an open state
     */
    public boolean isDrawerOpen() {
        return mDragOffset == 1;
    }

    /**
     * Kept for compatibility. {@see #openDrawer()}.
     *
     * @param gravity Ignored
     */
    @SuppressWarnings("UnusedParameters")
    public void openDrawer(int gravity) {
        openDrawer();
    }

    /**
     * Open the drawer animated.
     */
    public void openDrawer() {
        int drawerWidth = (int) (getWidth() * mMarginFactor);
        if (mViewDragHelper.smoothSlideViewTo(mContent, drawerWidth, mContent.getTop())) {
            ViewCompat.postInvalidateOnAnimation(SlidingDrawerLayout.this);
        }
    }

    /**
     * Kept for compatibility. {@see #closeDrawer()}.
     *
     * @param gravity Ignored
     */
    @SuppressWarnings("UnusedParameters")
    public void closeDrawer(int gravity) {
        closeDrawer();
    }

    /**
     * Close the drawer animated.
     */
    public void closeDrawer() {
        if (mViewDragHelper.smoothSlideViewTo(mContent, 0 - mContent.getPaddingLeft(), mContent.getTop())) {
            ViewCompat.postInvalidateOnAnimation(SlidingDrawerLayout.this);
        }
    }

    /**
     * Kept for compatibility. {@see #isDrawerVisible()}.
     *
     * @param gravity Ignored
     */
    @SuppressWarnings("UnusedParameters")
    public boolean isDrawerVisible(int gravity) {
        return isDrawerVisible();
    }

    /**
     * Check if the drawer is visible on the screen.
     *
     * @return true if the drawer is visible
     */
    public boolean isDrawerVisible() {
        return mDragOffset > 0;
    }

    /**
     * Enable or disable interaction the drawer.
     * <p/>
     * <p>This allows the application to restrict the user's ability to open or close
     * any drawer within this layout. SlidingDrawerLayout will still respond to calls to
     * {@link #openDrawer()}, {@link #closeDrawer()} and friends if a drawer is locked.</p>
     * <p/>
     * <p>Locking drawers open or closed will implicitly open or close
     * any drawers as appropriate.</p>
     *
     * @param lockMode The new lock mode. One of {@link #LOCK_MODE_UNLOCKED},
     *                 {@link #LOCK_MODE_LOCKED_CLOSED} or {@link #LOCK_MODE_LOCKED_OPEN}.
     * @see #LOCK_MODE_UNLOCKED
     * @see #LOCK_MODE_LOCKED_CLOSED
     * @see #LOCK_MODE_LOCKED_OPEN
     */
    public void setDrawerLockMode(@LockMode int lockMode) {
        mLockMode = lockMode;
        switch (lockMode) {
            case LOCK_MODE_LOCKED_CLOSED:
                mViewDragHelper.cancel();
                closeDrawer();
                break;
            case LOCK_MODE_LOCKED_OPEN:
                mViewDragHelper.cancel();
                openDrawer();
                break;
            case LOCK_MODE_UNLOCKED:
                break;
        }
    }

    /**
     * Set a listener to be notified of drawer events.
     *
     * @param drawerListener Listener to notify when drawer events occur
     * @see android.support.v4.widget.DrawerLayout.DrawerListener
     */
    public void setDrawerListener(android.support.v4.widget.DrawerLayout.DrawerListener drawerListener) {
        mDrawerListener = drawerListener;
    }

    public int getTopInset() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return 0;
        if (!mContent.getFitsSystemWindows()) return 0;

        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * @hide
     */
    @IntDef({STATE_IDLE, STATE_DRAGGING, STATE_SETTLING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {
    }

    /**
     * @hide
     */
    @IntDef({LOCK_MODE_UNLOCKED, LOCK_MODE_LOCKED_CLOSED, LOCK_MODE_LOCKED_OPEN})
    @Retention(RetentionPolicy.SOURCE)
    private @interface LockMode {
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return mLockMode == LOCK_MODE_UNLOCKED && child == mContent;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left < 0) return 0;
            int width = (int) (getWidth() * mMarginFactor);
            if (left > width) return width;
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return getTopInset();
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return SlidingDrawerLayout.this.getMeasuredWidth();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (xvel > 0 || xvel == 0 && mDragOffset > 0.5f) {
                openDrawer();
            } else {
                closeDrawer();
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            mDragOffset = map(left, 0, SlidingDrawerLayout.this.getWidth() * mMarginFactor, 0, 1);

            float scaleFactorContent = map(mDragOffset, 0, 1, mContentScaleClosed, mContentScaleOpen);
            mContent.setScaleX(scaleFactorContent);
            mContent.setScaleY(scaleFactorContent);

            float scaleFactorMenu = map(mDragOffset, 0, 1, mMenuScaleClosed, mMenuScaleOpen);
            mMenu.setScaleX(scaleFactorMenu);
            mMenu.setScaleY(scaleFactorMenu);

            float alphaValue = map(mDragOffset, 0, 1, mMenuAlphaClosed, mMenuAlphaOpen);
            mMenu.setAlpha(alphaValue);

            if (mDrawerListener != null) {
                mDrawerListener.onDrawerSlide(SlidingDrawerLayout.this, mDragOffset);
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);

            if (state == STATE_IDLE) {
                if (mDragOffset == 0) {
                    if (mDrawerListener != null) {
                        mDrawerListener.onDrawerClosed(SlidingDrawerLayout.this);
                    }
                } else if (mDragOffset == 1) {
                    if (mDrawerListener != null) {
                        mDrawerListener.onDrawerOpened(SlidingDrawerLayout.this);
                    }
                }
            }

            if (state != mDrawerState) {
                mDrawerState = state;

                if (mDrawerListener != null) {
                    mDrawerListener.onDrawerStateChanged(state);
                }
            }
        }
    }


    /**
     * Set the scale of the content when the drawer is closed. 1.0f is the original size.
     *
     * @param contentScaleClosed Scale of the content if the drawer is closed.
     */
    public void setContentScaleClosed(float contentScaleClosed) {
        mContentScaleClosed = contentScaleClosed;
        invalidate();
        requestLayout();
    }

    /**
     * Set the scale of the content when the drawer is open. 1.0f is the original size.
     *
     * @param contentScaleOpen Scale of the content when the drawer is open.
     */
    public void setContentScaleOpen(float contentScaleOpen) {
        mContentScaleOpen = contentScaleOpen;
        invalidate();
        requestLayout();
    }

    /**
     * Set the scale of the menu when the drawer is closed. 1.0f is the original size.
     *
     * @param menuScaleClosed Scale of the menu when the drawer is closed.
     */
    public void setMenuScaleClosed(float menuScaleClosed) {
        mMenuScaleClosed = menuScaleClosed;
        invalidate();
        requestLayout();
    }

    /**
     * Set the scale of the menu when the drawer is open. 1.0f is the original size.
     *
     * @param menuScaleOpen Scale of the menu when the drawer is open.
     */
    public void setMenuScaleOpen(float menuScaleOpen) {
        mMenuScaleOpen = menuScaleOpen;
        invalidate();
        requestLayout();
    }

    /**
     * Set the alpha of the menu when the drawer is closed.
     * 0.0f is transparent, 1.0f is completely visible.
     *
     * @param menuAlphaClosed Alpha of the menu when the drawer is closed.
     */
    public void setMenuAlphaClosed(float menuAlphaClosed) {
        mMenuAlphaClosed = menuAlphaClosed;
        invalidate();
        requestLayout();
    }

    /**
     * Set the alpha of the menu when the drawer is open.
     * 0.0f is transparent, 1.0f is completely visible.
     *
     * @param menuAlphaOpen Alpha of the menu when the drawer is open.
     */
    public void setMenuAlphaOpen(float menuAlphaOpen) {
        mMenuAlphaOpen = menuAlphaOpen;
        invalidate();
        requestLayout();
    }

    /**
     * Set the amount of space of the content visible when the drawer is opened.
     * 1.0f will move the drawer completely of the screen. The default value is 0.7f.
     *
     * @param marginFactor Amount of space of the content when drawer is open.
     */
    public void setMarginFactor(float marginFactor) {
        mMarginFactor = marginFactor;
        invalidate();
        requestLayout();
    }
}
