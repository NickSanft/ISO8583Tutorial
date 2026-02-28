package com.divora.views

import kotlinx.html.*

fun HTML.commonLayout(title: String, currentPath: String = "", content: MAIN.() -> Unit) {
    head {
        title(title)
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        style {
            unsafe {
                raw("""
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }

                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        min-height: 100vh;
                        padding: 20px;
                    }

                    nav {
                        background: rgba(255, 255, 255, 0.95);
                        padding: 15px;
                        border-radius: 10px;
                        margin-bottom: 20px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                        display: flex;
                        flex-wrap: wrap;
                        gap: 5px;
                        align-items: center;
                    }

                    nav a {
                        color: #667eea;
                        text-decoration: none;
                        margin-right: 15px;
                        font-weight: 600;
                        transition: color 0.3s;
                        padding: 4px 0;
                    }

                    nav a:hover {
                        color: #764ba2;
                    }

                    nav a.nav-active {
                        color: #764ba2;
                        border-bottom: 2px solid #764ba2;
                    }

                    main {
                        background: white;
                        padding: 40px;
                        border-radius: 10px;
                        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
                        max-width: 1200px;
                        margin: 0 auto;
                    }

                    h1 {
                        color: #333;
                        margin-bottom: 20px;
                        font-size: 2.5em;
                    }

                    h2 {
                        color: #667eea;
                        margin-top: 30px;
                        margin-bottom: 15px;
                        font-size: 1.8em;
                    }

                    h3 {
                        color: #764ba2;
                        margin-top: 20px;
                        margin-bottom: 10px;
                        font-size: 1.4em;
                    }

                    p {
                        line-height: 1.6;
                        margin-bottom: 15px;
                        color: #555;
                    }

                    code {
                        background: #f4f4f4;
                        padding: 2px 6px;
                        border-radius: 3px;
                        font-family: 'Courier New', monospace;
                        color: #e83e8c;
                    }

                    pre {
                        background: #2d2d2d;
                        color: #f8f8f2;
                        padding: 20px;
                        border-radius: 5px;
                        overflow-x: auto;
                        margin: 8px 0 20px 0;
                        font-family: 'Courier New', monospace;
                    }

                    .pre-wrapper {
                        margin: 20px 0;
                    }

                    .copy-btn {
                        background: #555;
                        color: white;
                        padding: 4px 12px;
                        border: none;
                        border-radius: 4px;
                        font-size: 0.8em;
                        cursor: pointer;
                        float: right;
                        margin-bottom: 4px;
                        transition: background 0.2s;
                        transform: none !important;
                        box-shadow: none !important;
                        font-weight: normal;
                    }

                    .copy-btn:hover {
                        background: #333;
                        transform: none !important;
                        box-shadow: none !important;
                    }

                    .example-box {
                        background: #f8f9fa;
                        border-left: 4px solid #667eea;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }

                    .bitmap-visual {
                        font-family: 'Courier New', monospace;
                        background: #2d2d2d;
                        color: #4ec9b0;
                        padding: 15px;
                        border-radius: 5px;
                        overflow-x: auto;
                        margin: 15px 0;
                    }

                    .form-group {
                        margin-bottom: 20px;
                    }

                    label {
                        display: block;
                        margin-bottom: 5px;
                        font-weight: 600;
                        color: #333;
                    }

                    input, textarea {
                        width: 100%;
                        padding: 10px;
                        border: 2px solid #ddd;
                        border-radius: 5px;
                        font-size: 16px;
                        transition: border-color 0.3s;
                    }

                    input:focus, textarea:focus {
                        outline: none;
                        border-color: #667eea;
                    }

                    .field-hint {
                        font-size: 0.9em;
                        color: #666;
                        margin-top: 5px;
                    }

                    button {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 12px 30px;
                        border: none;
                        border-radius: 5px;
                        font-size: 16px;
                        font-weight: 600;
                        cursor: pointer;
                        transition: transform 0.2s, box-shadow 0.2s;
                    }

                    button:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
                    }

                    .success {
                        background: #d4edda;
                        border-left: 4px solid #28a745;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }

                    .error {
                        background: #f8d7da;
                        border-left: 4px solid #dc3545;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }

                    .warning {
                        background: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }

                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin: 20px 0;
                    }

                    th, td {
                        padding: 12px;
                        text-align: left;
                        border-bottom: 1px solid #ddd;
                    }

                    th {
                        background: #667eea;
                        color: white;
                        font-weight: 600;
                    }

                    tr:hover {
                        background: #f8f9fa;
                    }

                    .page-nav {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-top: 40px;
                        padding-top: 20px;
                        border-top: 2px solid #eee;
                    }

                    .page-nav a {
                        display: inline-block;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 10px 20px;
                        border-radius: 5px;
                        text-decoration: none;
                        font-weight: 600;
                        transition: transform 0.2s, box-shadow 0.2s;
                    }

                    .page-nav a:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
                        color: white;
                    }

                    .page-nav-spacer {
                        display: block;
                        width: 1px;
                    }
                """)
            }
        }
        script {
            unsafe {
                raw("""
                    function copyToClipboard(elementId, btn) {
                        var text = document.getElementById(elementId).innerText;
                        navigator.clipboard.writeText(text).then(function() {
                            btn.textContent = 'Copied!';
                            setTimeout(function() { btn.textContent = 'Copy'; }, 2000);
                        });
                    }
                """)
            }
        }
    }
    body {
        nav {
            listOf(
                "/" to "Home",
                "/tutorial/introduction" to "Introduction",
                "/tutorial/mti" to "MTI",
                "/tutorial/bitmap" to "Bitmap",
                "/tutorial/fields" to "Fields",
                "/tutorial/variable-length" to "Field Formats",
                "/tutorial/flow" to "Message Flow",
                "/simulator" to "Simulator"
            ).forEach { (href, label) ->
                a(href = href, classes = if (href == currentPath) "nav-active" else null) { +label }
            }
        }
        main {
            content()
        }
    }
}
