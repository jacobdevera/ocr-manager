/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gjdevera.ocrreader;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.gjdevera.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.List;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class OcrGraphic extends GraphicOverlay.Graphic {

    private int mId;

    private static final int TEXT_COLOR = Color.WHITE;
    private static final float SCALAR = (float) 0.7;

    private static Paint sRectPaint;
    private static Paint sTextPaint;
    private final TextBlock mText;

    OcrGraphic(GraphicOverlay overlay, TextBlock text) {
        super(overlay);

        mText = text;

        if (sRectPaint == null) {
            sRectPaint = new Paint();
            sRectPaint.setColor(TEXT_COLOR);
            sRectPaint.setStyle(Paint.Style.STROKE);
            sRectPaint.setStrokeWidth(4.0f);
        }

        if (sTextPaint == null) {
            sTextPaint = new Paint();
            sTextPaint.setColor(TEXT_COLOR);
            sTextPaint.setTextSize(54.0f);
        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public TextBlock getTextBlock() {
        return mText;
    }

    /**
     * Checks whether a point is within the bounding box of this graphic.
     * The provided point should be relative to this graphic's containing overlay.
     * @param x An x parameter in the relative context of the canvas.
     * @param y A y parameter in the relative context of the canvas.
     * @return True if the provided point is contained within this graphic's bounding box.
     */
    public boolean contains(float x, float y) {
        if (mText == null) {
            return false;
        }
        RectF rect = createBoundingBox(new RectF(mText.getBoundingBox()));
        return (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y);
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        if (mText == null) {
            return;
        }

        // Draws the bounding box around the TextBlock.
        RectF rect = createBoundingBox(new RectF(mText.getBoundingBox()));
        canvas.drawRect(rect, sRectPaint);

        // Break the text into multiple lines (and lines into words)
        // and draw each one according to its own bounding box.
        List<? extends Text> textComponents = mText.getComponents();
        sTextPaint.setTextSize((rect.height() / textComponents.size()) * SCALAR);
        for(Text currentText : textComponents) {
            Rect boundingBox = currentText.getBoundingBox();
            float left = translateX(boundingBox.left);
            float bottom = translateY(boundingBox.bottom);
            canvas.drawText(currentText.getValue(), left, bottom, sTextPaint);
        }
    }

    private RectF createBoundingBox(RectF boundingBox) {
        boundingBox.left = translateX(boundingBox.left);
        boundingBox.top = translateY(boundingBox.top);
        boundingBox.right = translateX(boundingBox.right);
        boundingBox.bottom = translateY(boundingBox.bottom);
        return boundingBox;
    }
}
