/*
 * Copyright 2014 Alex Curran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.amlcurran.showcaseview;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public interface ShowcaseDrawer {

    boolean isCircleStyle();

    /* Getter & Setters region */

    void setShowcaseColour(int color);

    void setBackgroundColour(int backgroundColor);

    int getShowcaseWidth();

    void setShowcaseWidth(int width);

    int getShowcaseHeight();

    void setShowcaseHeight(int height);

    float getBlockedRadius();

    void setBlockedRadius(float radius);

    /* Drawing region */

    void erase(Bitmap bitmapBuffer);

    void drawShowcase(Bitmap buffer, float x, float y, float scaleMultiplier);

    void drawToCanvas(Canvas canvas, Bitmap bitmapBuffer);
}
