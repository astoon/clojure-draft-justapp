/*
 * justapp.util
 */

var justapp = (function (justapp) {

    function XHR() {
	return new XMLHttpRequest();
    }

    function GET(data) {
	var url = data.url,
	    callback = data.callback;
	var xhr = XHR();
	justapp.util.spinner(true);
	xhr.open('GET', url, true);
	xhr.onreadystatechange = function () {
	    if (xhr.readyState == 4 && xhr.status == 200) {
		callback(xhr.responseText);
		justapp.util.spinner(false);
	    }
	}
	xhr.send();
    }

    function PUT(data) {
	var url = data.url,
	    body = data.body,
	    contentType = data.contentType || 'text/plain;charset=UTF-8',
	    status = data.status || 200,
	    callback = data.callback || function () {};
	var xhr = XHR();
	xhr.open('PUT', url, true);
	xhr.setRequestHeader('content-type', contentType);
	xhr.onreadystatechange = function () {
	    if (xhr.readyState == 1) {
		justapp.util.spinner(true);
	    } else if (xhr.readyState == 4 && xhr.status == status) {
		callback(xhr.responseText);
		justapp.util.spinner(false);
	    }
	}
	xhr.send(body);
    }

    function POST(data) {
	var url = data.url,
	    body = data.body || '',
	    contentType = data.contentType || 'application/x-www-form-urlencoded;charset=UTF-8',
	    status = data.status || 200,
	    callback = data.callback || function () {};
	var xhr = XHR();
	justapp.util.spinner(true);
	xhr.open('POST', url, true);
	xhr.setRequestHeader('content-type', contentType);
	xhr.onreadystatechange = function () {
	    if (xhr.readyState == 4 && xhr.status == status) {
		callback(xhr.responseText);
		justapp.util.spinner(false);
	    }
	}
	xhr.send(body);
    }

    function DELETE(data) {
	var url = data.url,
	    callback = data.callback || function () {};
	var xhr = XHR();
	justapp.util.spinner(true);
	xhr.open('DELETE', url, true);
	xhr.onreadystatechange = function () {
	    if (xhr.readyState == 4 && xhr.status == 204) {
		callback();
		justapp.util.spinner(false);
	    }
	}
	xhr.send();
    }

    function insertAfter (parent, ref, el) {
	var next = ref.nextSibling;
	if (next) {
	    return parent.insertBefore(el, next);
	} else {
	    return parent.appendChild(el);
	}
    }

    justapp.util = {
	GET: GET,
	PUT: PUT,
	POST: POST,
	DELETE: DELETE,
	insertAfter: insertAfter,

	spinner: (function () {
	    var c = 0;
	    return function (isShow) {
		c = (isShow) ? c + 1 : c -1;
		c = (c < 0) ? 0 : c;
		document.getElementById('spinner').style.display = (c > 0) ? 'block' : 'none';
	    }
	}()),

    }

    return justapp;
}(justapp || {}));
