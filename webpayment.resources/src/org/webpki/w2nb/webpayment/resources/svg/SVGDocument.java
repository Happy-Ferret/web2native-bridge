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

import java.util.Vector;

public abstract class SVGDocument {

    static Vector<SVGObject> svgObjects = new Vector<SVGObject>();
    
    public abstract double getWidth();
    
    public abstract double getHeight();
    
    public abstract void generate();

    public SVGObject add(SVGObject svgObject) {
        svgObjects.add(svgObject);
        return svgObject;
    }

    public SVGAnchor createDocumentAnchor(double x,double y, SVGAnchor.ALIGNMENT alignment) {
        return new SVGAnchor(new SVGDoubleValue(x), new SVGDoubleValue(y), alignment);
    }
}
