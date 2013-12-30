/*
 * Copyright (c) 2013 Santiago M. Mola <santi@mola.io>
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a
 *   copy of this software and associated documentation files (the "Software"),
 *   to deal in the Software without restriction, including without limitation
 *   the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *   and/or sell copies of the Software, and to permit persons to whom the
 *   Software is furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *   OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 *   DEALINGS IN THE SOFTWARE.
 */

package io.mola.galimatias;

import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.PotentialAssignment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class TestURL {

    private String original;
    private String result;
    private String base;
    private boolean validURI;
    private String resultForRFC3986;
    private String resultForRFC2396;

    TestURL(String original) {
        this(null, original, original);
    }

    TestURL(String original, String result) {
        this(null, original, result);
    }

    TestURL(String base, String original, String result) {
        this.base = base;
        this.original = original;
        this.result = result;
        this.resultForRFC2396 = null;
        this.resultForRFC3986 = null;
        this.validURI = true;
    }

    String original() {
        return original;
    }

    String result() {
        return result;
    }

    URL base() throws MalformedURLException {
        if (base == null) {
            return null;
        }
        return URL.parse(base);
    }

    boolean isValidURI() {
        return validURI;
    }

    TestURL validURI(boolean validURI) {
        this.validURI = validURI;
        return this;
    }

    TestURL resultForRFC2396(String result) {
        this.resultForRFC2396 = result;
        return this;
    }

    String resultForRFC2396() {
        return nvl(resultForRFC2396, resultForRFC3986, result);
    }

    TestURL resultForRFC3986(String result) {
        this.resultForRFC3986 = result;
        return this;
    }

    String resultForRFC3986() {
        return nvl(resultForRFC3986, result);
    }

    private static String nvl(String... values) {
        for (final String value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("TestURL(original='%s', base='%s'", result, base);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @ParametersSuppliedBy(TestURLSupplier.class)
    public static @interface TestURLs {

    }

    public static class TestURLSupplier extends ParameterSupplier {
        @Override
        public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
            List<PotentialAssignment> values = new ArrayList<PotentialAssignment>();
            for (final TestURL testURL : TEST_URLS) {
                values.add(PotentialAssignment.forValue(testURL.toString(), testURL));
            }
            return values;
        }
    }

    public static final TestURL[] TEST_URLS = new TestURL[] {
            // basic
            new TestURL("http://example.com/"),
            new TestURL("http://example.com", "http://example.com/"),

            // leading spaces
            new TestURL("  http://example.com/", "http://example.com/"),

            // ip host
            new TestURL("http://1.1.1.1/"),
            new TestURL("http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]/", "http://[fedc:ba98:7654:3210:fedc:ba98:7654:3210]/"),

            // default port
            new TestURL("https://example.com:443/", "https://example.com/"),
            new TestURL("ftp://example.com:80/"),

            // user info
            new TestURL("http://user:pass@example.com/"),
            new TestURL("http://user@example.com/"),
            new TestURL("http://user:@example.com/"),
            new TestURL("http://:pass@example.com/"),
            new TestURL("http://:@example.com/"),

            // path
            new TestURL("http://example.com/foo"),
            new TestURL("http://example.com/foo/"),
            new TestURL("http://example.com/foo/../bar", "http://example.com/bar"),
            new TestURL("http://example.com/foo/%2E%2e/bar", "http://example.com/bar"),
            new TestURL("http://example.com/foo/%2E./bar", "http://example.com/bar"),
            new TestURL("http://example.com/foo/./bar", "http://example.com/foo/bar"),
            new TestURL("http://example.com/./bar", "http://example.com/bar"),

            // query
            new TestURL("http://example.com/?foo=1"),
            new TestURL("http://example.com/?foo%c3%9f", "http://example.com/?foo%c3%9f"),
            new TestURL("http://example.com/?fooß", "http://example.com/?foo%C3%9F"),

            // fragment
            new TestURL("http://example.com/#foo"),
            new TestURL("http://example.com/?foo=1#foo"),
            new TestURL("http://example.com/?foo=1#"),
            new TestURL("http://example.com/#foo%c3%9f", "http://example.com/#foo%c3%9f"),
            new TestURL("http://example.com/#fooß", "http://example.com/#foo%C3%9F"),

            // Relative to base
            new TestURL("http://example.com", "/foo", "http://example.com/foo"),
            new TestURL("http://example.com", "foo", "http://example.com/foo"),
            new TestURL("http://example.com/foo", "bar", "http://example.com/bar"),
            new TestURL("http://example.com/foo/", "bar", "http://example.com/foo/bar"),
            new TestURL("http://example.com/foo/bar", "/baz", "http://example.com/baz"),
            new TestURL("http://example.com/foo/bar/", "/baz", "http://example.com/baz"),
            new TestURL("http://example.com", "?foo", "http://example.com/?foo"),
            new TestURL("http://example.com", "//other.com", "http://other.com/"),
            new TestURL("https://example.com", "//other.com", "https://other.com/"),
            new TestURL("https://example.com", "http://other.com", "http://other.com/"),

            // Unicode
            new TestURL("http://example.com/\uD801\uDC00", "http://example.com/%F0%90%90%80"),
            new TestURL("http://example.com/\uD83D\uDC35", "http://example.com/%F0%9F%90%B5"),

            // IDN
            new TestURL("http://ジェーピーニック.jp", "http://xn--hckqz9bzb1cyrb.jp/"),
            new TestURL("http://☃.com/", "http://xn--n3h.com/"),
            new TestURL("http://☃☃☃.com/", "http://xn--n3haa.com/"),
            new TestURL("http://\uD83D\uDC35.com/", "http://xn--9o8h.com/"),

            // tilde
            new TestURL("http://example.com/~user"),


            // unwise characters   "{", "}", "|", "\", "^", "~", "[", "]", and "`".
            new TestURL("http://example.com/^{}|[]`", "http://example.com/^{}|[]%60")
                    .resultForRFC2396("http://example.com/%5E%7B%7D%7C%5B%5D%60"),
            new TestURL("http://example.com/?^{}|[]`", "http://example.com/?^{}|[]%60")
                    .resultForRFC2396("http://example.com/?%5E%7B%7D%7C%5B%5D%60"),
            new TestURL("http://example.com/#^{}|[]`", "http://example.com/#^{}|[]`")
                    .resultForRFC2396("http://example.com/#%5E%7B%7D%7C%5B%5D%60"),

            // file:
            new TestURL("file://localhost/etc/fstab"),
            new TestURL("file:////etc/fstab"),
            new TestURL("file:///c:/WINDOWS/clock.avi", "file:///c:/WINDOWS/clock.avi"),
            new TestURL("file:///c|/WINDOWS/clock.avi", "file:///c:/WINDOWS/clock.avi")
                    .resultForRFC2396("file:///c%7C/WINDOWS/clock.avi"), //FIXME: This is not consistent with the previous case
            new TestURL("file://localhost/c|/WINDOWS/clock.avi", "file://localhost/c:/WINDOWS/clock.avi")
                    .resultForRFC2396("file://localhost/c%7C/WINDOWS/clock.avi"), //XXX: Not sure this is correct
            new TestURL("file://localhost/c:/WINDOWS/clock.avi", "file://localhost/c:/WINDOWS/clock.avi"),

            // data:
            new TestURL("data:foo"),

            // Uncommon schemes

            //XXX: 'aaa' URIs are not standard anymore as of RFC 3986.
            //           java.net.URI can parse them fairly well anyway.
            //new TestURL("aaa://host.example.com:1813;transport=udp;protocol=rad"),

            new TestURL("about:blank"),
            new TestURL("adiumxtra://www.adiumxtras.com/download/0000"),
            new TestURL("aim:goim?screenname=notarealuser&message=This+is+my+message"),
            new TestURL("apt:gcc"),
            new TestURL("callto:+34600800900"),
            //FIXME: new TestURL("ed2k://|file|The_Two_Towers-The_Purist_Edit-Trailer.avi|14997504|965c013e991ee246d63d45ea71954c4d|/|sources,202.89.123.6:4662|/").validURI(false),
            new TestURL("feed:https://example.com/rss.xml"),
            new TestURL("magnet:?xt=urn:sha1:YNCKHTQCWBTRNJIV4WNAE52SJUQCZO5C"),
            new TestURL("mailto:user@example.com"),

            new TestURL("chrome-extension://ognampngfcbddbfemdapefohjiobgbdl/monitor.html?tabId=41&browserId=0")
    };

}
