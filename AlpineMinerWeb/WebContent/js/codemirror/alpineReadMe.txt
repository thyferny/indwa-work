The code in this directory is from http://codemirror.net/

Version is: 2.34.

codemirror is an source code editor that formats Pig and SQL nicely.


codemirror-compressed.js includes:

    <%--<script src="../../js/codemirror-2.34/lib/codemirror.js"></script>--%>
    <%--<script src="../../js/codemirror-2.34/lib/util/simple-hint.js"></script>--%>
    <%--<script src="../../js/codemirror-2.34/lib/util/pig-hint.js"></script>--%>
    <%--<script src="../../js/codemirror-2.34/mode/plsql/plsql.js"></script>--%>
    <%--<script src="../../js/codemirror-2.34/mode/pig/pig.js"></script>--%>

These are all scripts from the site.

It also includes plsql-hint.js which is our own script based on the pig-hint.js.


codemirror-2.34/doc/compress.html is included in the codemirror-2.34 download and was used to create codemirror-compressed.js.


simple-hint.css and codemirror.css are copied from the lib and lib/util directory

Note that you need to change the style a little bit:

    <style>.CodeMirror {border: 1px inset #dee;}</style>
    <style>.CodeMirror-completions {z-index: 1000;}</style>

These changes can be put directly in the jsp file.