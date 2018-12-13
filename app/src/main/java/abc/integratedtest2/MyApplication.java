package abc.integratedtest2;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by HAN on 2017-11-16.
 */

public class MyApplication extends Application {
    private Context dialogContext;
    private ArrayList<Bitmap> singleBitmaps;
    private ArrayList<Bitmap> setBitmaps;
    private ArrayList<Bitmap> sideBitmaps;

    public Context getDialogContext() {
        return dialogContext;
    }

    public void setDialogContext(Context dialogContext) {
        this.dialogContext = dialogContext;
    }

    // 싱글
    public ArrayList<Bitmap> getSingleBitmaps() {
        return singleBitmaps;
    }

    public void setSingleBitmaps(ArrayList<Bitmap> bitmaps) {
        this.singleBitmaps = bitmaps;
    }

    public void addSingleBitsmaps(Bitmap bitmap) {
        singleBitmaps.add(bitmap);
    }

    // 세트
    public ArrayList<Bitmap> getSetBitmaps() {
        return setBitmaps;
    }

    public void setSetBitmaps(ArrayList<Bitmap> setBitmaps) {
        this.setBitmaps = setBitmaps;
    }

    public void addSetBitsmaps(Bitmap bitmap) {
        setBitmaps.add(bitmap);
    }

    // 사이드
    public ArrayList<Bitmap> getSideBitmaps() {
        return sideBitmaps;
    }

    public void setSideBitmaps(ArrayList<Bitmap> sideBitmaps) {
        this.sideBitmaps = sideBitmaps;
    }

    public void addSideBitsmaps(Bitmap bitmap) {
        sideBitmaps.add(bitmap);
    }

    // 내용 초기화
    public void reset() {
        singleBitmaps.clear();
        singleBitmaps.trimToSize();
        setBitmaps.clear();
        setBitmaps.trimToSize();
        sideBitmaps.clear();
        sideBitmaps.trimToSize();
    }
}
