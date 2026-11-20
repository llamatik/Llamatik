/* Download page: highlight the visitor's platform and point desktop buttons at the latest release. */
(function () {
  'use strict';
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var $$ = function (s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); };
  var LT = window.LT || {};
  var REPO = 'ferranpons/Llamatik';

  /* recommend the visitor's platform */
  var ua = navigator.userAgent || '', p = navigator.platform || '', os = 'web';
  if (/android/i.test(ua)) os = 'android';
  else if (/iphone|ipad|ipod/i.test(ua) || (p === 'MacIntel' && navigator.maxTouchPoints > 1)) os = 'ios';
  else if (/win/i.test(p)) os = 'win';
  else if (/mac/i.test(p)) os = 'mac';
  else if (/linux|x11/i.test(p + ua)) os = 'linux';
  if (os !== 'web') {
    var card = $('.dl-card[data-os="' + os + '"]');
    if (card) { card.classList.add('rec'); card.setAttribute('data-rec', LT.rec || ''); }
  }

  /* latest release -> real asset links, checksums and version */
  function apply(rel) {
    if (!rel || !rel.assets) return;
    $$('[data-ext]').forEach(function (a) {
      var ext = '.' + a.dataset.ext;
      var hit = rel.assets.filter(function (x) { return x.name.toLowerCase().slice(-ext.length) === ext; })[0];
      if (hit) a.href = hit.url;
    });
    $$('[data-sum]').forEach(function (a) {
      var key = a.dataset.sum;
      var hit = rel.assets.filter(function (x) {
        var n = x.name.toLowerCase();
        return n.indexOf('sha256') !== -1 && n.indexOf(key) !== -1;
      })[0];
      if (hit) a.href = hit.url; else a.hidden = true;
    });
    var v = $('#relVersion');
    if (v && rel.tag) {
      v.textContent = (LT.version || 'Version {v}').replace('{v}', rel.tag.replace(/^v/, ''));
      v.hidden = false;
    }
  }

  var cached = null;
  try { cached = JSON.parse(sessionStorage.getItem('lt_rel') || 'null'); } catch (e) {}
  if (cached) apply(cached);
  if (!cached && window.fetch) {
    fetch('https://api.github.com/repos/' + REPO + '/releases/latest')
      .then(function (r) { return r.ok ? r.json() : null; })
      .then(function (j) {
        if (!j || !j.assets) return;
        var rel = { tag: j.tag_name, assets: j.assets.map(function (a) { return { name: a.name, url: a.browser_download_url }; }) };
        try { sessionStorage.setItem('lt_rel', JSON.stringify(rel)); } catch (e) {}
        apply(rel);
      }).catch(function () {});
  }
})();
