package net.i2p.router.web;

import java.io.IOException;
import net.i2p.data.DataHelper;

public class NetDbHelper extends HelperBase {
    private String _routerPrefix;
    private String _version;
    private String _country;
    private int _full;
    private boolean _lease;
    private boolean _debug;
    private boolean _graphical;
    
    private static final String titles[] =
                                          {_x("Summary"),                       // 0
                                           _x("Local Router"),                  // 1
                                           _x("Router Lookup"),                 // 2
                                           _x("All Routers"),                   // 3
                                           _x("All Routers with Full Stats"),   // 4
                                           "LeaseSet Debug",                    // 5
                                           _x("LeaseSets"),                     // 6
                                           "Sybil"   };                         // 7

    private static final String links[] =
                                          {"",                                  // 0
                                           "?r=.",                              // 1
                                           "",                                  // 2
                                           "?f=2",                              // 3
                                           "?f=1",                              // 4
                                           "?l=2",                              // 5
                                           "?l=1",                              // 6
                                           "?f=3" };                            // 7

    public void setRouter(String r) {
        if (r != null)
            _routerPrefix = DataHelper.stripHTML(r);  // XSS
    }

    /** @since 0.9.21 */
    public void setVersion(String v) {
        if (v != null)
            _version = DataHelper.stripHTML(v);  // XSS
    }

    /** @since 0.9.21 */
    public void setCountry(String c) {
        if (c != null)
            _country = DataHelper.stripHTML(c);  // XSS
    }

    public void setFull(String f) {
        try {
            _full = Integer.parseInt(f);
        } catch (NumberFormatException nfe) {}
    }

    public void setLease(String l) {
        _debug = "2".equals(l);
        _lease = _debug || "1".equals(l);
    }
    
    /**
     *  call for non-text-mode browsers
     *  @since 0.9.1
     */
    public void allowGraphical() {
        _graphical = true;
    }

    /**
     *   storeWriter() must be called previously
     */
    public String getNetDbSummary() {
        NetDbRenderer renderer = new NetDbRenderer(_context);
        try {
            renderNavBar();
            if (_routerPrefix != null || _version != null || _country != null)
                renderer.renderRouterInfoHTML(_out, _routerPrefix, _version, _country);
            else if (_lease)
                renderer.renderLeaseSetHTML(_out, _debug);
            else if (_full == 3)
                (new SybilRenderer(_context)).getNetDbSummary(_out);
            else
                renderer.renderStatusHTML(_out, _full);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return "";
    }

    /**
     *  @since 0.9.1
     */
    private int getTab() {
        if (_debug)
            return 5;
        if (_lease)
            return 6;
        if (".".equals(_routerPrefix))
            return 1;
        if (_routerPrefix != null)
            return 2;
        if (_full == 2)
            return 3;
        if (_full == 1)
            return 4;
        if (_full == 3)
            return 7;
        return 0;
    }

    /**
     *  @since 0.9.1
     */
    private void renderNavBar() throws IOException {
        StringBuilder buf = new StringBuilder(1024);
        buf.append("<div class=\"confignav\" id=\"confignav\">");
        // TODO fix up the non-light themes
        String theme = _context.getProperty(CSSHelper.PROP_THEME_NAME);
        boolean span = _graphical && (theme == null || theme.equals(CSSHelper.DEFAULT_THEME));
        if (!span)
            buf.append("<center>");
        int tab = getTab();
        for (int i = 0; i < titles.length; i++) {
            if (i == 2 && tab != 2)
                continue;   // can't nav to lookup
            if ((i == 5 || i == 7) && !_context.getBooleanProperty(PROP_ADVANCED))
                continue;
            if (i == tab) {
                // we are there
                if (span)
                    buf.append("<span class=\"tab2\">");
                buf.append(_t(titles[i]));
            } else {
                // we are not there, make a link
                if (span)
                    buf.append("<span class=\"tab\">");
                buf.append("<a href=\"netdb").append(links[i]).append("\">").append(_t(titles[i])).append("</a>");
            }
            if (span)
                buf.append(" </span>\n");
            else if (i != titles.length - 1)
                buf.append(" |\n");
        }
        if (!span)
            buf.append("</center>");
        buf.append("</div>");
        _out.write(buf.toString());
    }
}
