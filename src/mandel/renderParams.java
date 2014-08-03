package mandel;

import org.apfloat.Apfloat;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by user on 8/2/14.
 */
public class renderParams implements Serializable {
    Apfloat minI, maxI;
    Apfloat minR, maxR;
    int quald;
    boolean usebignums;
    int itermaximum;

    double maxx;
    double maxy;
    double minx;
    double miny;

    public renderParams(Apfloat _minI, Apfloat _maxI, Apfloat _minR, Apfloat _maxR,
                        double _maxx, double _maxy, double _minx, double _miny,
                        int _quald, boolean _usebignums, int _itermaximum, int scale){

        minI = new Apfloat(0, scale+30).add(_minI);
        maxI = new Apfloat(0, scale+30).add(_maxI);
        minR = new Apfloat(0, scale+30).add(_minR);
        maxR = new Apfloat(0, scale+30).add(_maxR);

        quald = _quald;
        usebignums = _usebignums;
        itermaximum = _itermaximum;

        maxx=_maxx;
        maxy=_maxy;
        minx=_minx;
        miny=_miny;
    }
}
