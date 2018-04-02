# SlidingContentDrawer

This Android library provides an easy way to create an alternative navigation
drawer for android. Instead of a drawer that slides over the main content of
the Activity, this lets the content slide away and reveal a menu below it.

By default it applies a scaling effect on the content and menu.

## Demo
![Demo](/demo.gif?raw=true)

The demo app is included in the `app` module in this project.

## Usage

### 1. Include the library
Add the dependency to your Gradle file:
```
dependencies {
    ...
    compile 'com.wouterhabets:slidingcontentdrawer:1.0'
}
```

### 2. Add the `SlidingContentDrawer` view

1. Add a `SlidingContentDrawer` to your Activity.
2. Add a View which contains your menu. Give this the tag `menu`.
3. Add a View which contains your content. Give this the tag `content`.

The `SlidingContentDrawer` will use the tags `menu` and `content` to identify the content and menu. So any other view will be ignored. So you can add for example an `ImageView` below the menu to display a background in the drawer.

```xml
<com.wouterhabets.slidingcontentdrawer.widget.SlidingDrawerLayout
    android:id="@+id/drawer_layout"
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?attr/colorPrimaryDark"
    android:fitsSystemWindows="true">

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:tag="menu">

        <include layout="@layout/drawer_menu"/>

    </FrameLayout>

    <android.support.design.widget.CoordinatorLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="?android:colorBackground"
        android:elevation="6dp"
        android:fitsSystemWindows="true"
        android:tag="content">

        <include layout="@layout/content_activity_main"/>

    </android.support.design.widget.CoordinatorLayout>

</com.wouterhabets.slidingcontentdrawer.widget.SlidingDrawerLayout>
```

### 3. Initialize the drawer view

The API of the `SlidingDrawerLayout` is mostly the same as the original `DrawerLayout` from the Android design library. Same for `SlidingDrawerToggle` which is a modified version of the `ActionBarDrawerToggle` to support the `SlidingDrawerLayout`.

```Java
SlidingDrawerLayout drawer = (SlidingDrawerLayout) findViewById(R.id.drawer_layout);
SlidingDrawerToggle toggle = new SlidingDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
drawer.setDrawerListener(toggle);
toggle.syncState();
```

### 4. Customize the SlidingDrawerLayout

All values are `Float` values. The default values are used in the example.

#### Content scaling effect
The scaling applied on the content when sliding it from left to right.
```xml
app:contentScaleClosed="1.0"
app:contentScaleOpen="0.7"
```

#### Menu scaling effect
The scaling applied on the menu when sliding the content from left to right.
```xml
app:menuScaleClosed="1.1"
app:menuScaleOpen="1.0"
```

#### Menu alpha effect
The alpha on the menu when sliding the content from left to right.
```xml
app:menuAlphaClosed="0.0"
app:menuAlphaOpen="1.0"
```

#### Content margin factor
This value is used to calculate how much of the content should be visible when the content is slided to the right. This is calculated with the width of the `SlidingDrawerLayoutWhen`: `getWidth * marginFactor`. So setting this to 1.0f will slide the content out of the activity. The default is 0.7f.

```xml
app:marginFactor="0.7"
```
