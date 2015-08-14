/*
 *  Copyright 2006-2015 WebPKI.org (http://webpki.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.webpki.w2nb.webpayment.resources.svg;

public class SVGLine extends SVGObject {

    public SVGLine(SVGValue x1,
                   SVGValue y1,
                   SVGValue x2,
                   SVGValue y2,
                   SVGValue strokeWidth,
                   SVGValue strokeColor) {
        addDouble(SVGAttributes.X1, x1);
        addDouble(SVGAttributes.Y1, y1);
        addDouble(SVGAttributes.X2, x2);
        addDouble(SVGAttributes.Y2, y2);
        addDouble(SVGAttributes.STROKE_WIDTH, strokeWidth);
        addString(SVGAttributes.STROKE_COLOR, strokeColor);
    }

    @Override
    SVGValue getAnchorX() {
        return getAttribute(SVGAttributes.X1);
    }
    
    @Override
    SVGValue getAnchorY() {
        return getAttribute(SVGAttributes.Y1);
    }
    
    @Override
    String getTag() {
        return "line";
    }

    @Override
    boolean hasBody() {
        return false;
    }
}
