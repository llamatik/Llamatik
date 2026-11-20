/* Llamatik landing page behaviour. No dependencies. */
(function () {
  'use strict';
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var $$ = function (s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); };
  var reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var WEB_APP = (window.LT && window.LT.prompt) || '/prompt/';
  var REPO = 'ferranpons/Llamatik';

  /* ---------- toast ---------- */
  var toastEl = $('#toast'), toastT;
  function toast(msg) {
    toastEl.textContent = msg;
    toastEl.classList.add('show');
    clearTimeout(toastT);
    toastT = setTimeout(function () { toastEl.classList.remove('show'); }, 2200);
  }
  function copy(text) {
    if (navigator.clipboard && window.isSecureContext) {
      return navigator.clipboard.writeText(text).then(function () { return true; }, function () { return false; });
    }
    var ta = document.createElement('textarea');
    ta.value = text; ta.style.position = 'fixed'; ta.style.opacity = '0';
    document.body.appendChild(ta); ta.select();
    var ok = false;
    try { ok = document.execCommand('copy'); } catch (e) {}
    document.body.removeChild(ta);
    return Promise.resolve(ok);
  }

  /* ---------- nav ---------- */
  var nav = $('#nav'), burger = $('#burger'), links = $('#navLinks');
  function onScroll() { nav.classList.toggle('scrolled', window.scrollY > 12); }
  onScroll(); window.addEventListener('scroll', onScroll, { passive: true });
  burger.addEventListener('click', function () {
    var open = links.classList.toggle('open');
    burger.setAttribute('aria-expanded', open);
  });
  $$('a', links).forEach(function (a) {
    a.addEventListener('click', function () { links.classList.remove('open'); burger.setAttribute('aria-expanded', 'false'); });
  });

  /* ---------- language menu ---------- */
  var langBtn = $('#langBtn'), langMenu = $('#langMenu');
  if (langBtn && langMenu) {
    var closeLang = function () { langMenu.hidden = true; langBtn.setAttribute('aria-expanded', 'false'); };
    langBtn.addEventListener('click', function (e) {
      e.stopPropagation();
      var open = langMenu.hidden;
      langMenu.hidden = !open;
      langBtn.setAttribute('aria-expanded', open);
    });
    document.addEventListener('click', closeLang);
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape') closeLang(); });
  }

  /* ---------- pointer glow ---------- */
  var spot = $('#spot');
  if (!reduced && spot && window.matchMedia('(pointer:fine)').matches) {
    window.addEventListener('pointermove', function (e) {
      spot.style.left = e.clientX + 'px';
      spot.style.top = (e.clientY + window.scrollY * 0) + 'px';
    }, { passive: true });
  }
  $$('.card, .dl-card').forEach(function (c) {
    c.addEventListener('pointermove', function (e) {
      var r = c.getBoundingClientRect();
      c.style.setProperty('--mx', (e.clientX - r.left) + 'px');
      c.style.setProperty('--my', (e.clientY - r.top) + 'px');
    });
  });

  /* ---------- reveal on scroll ---------- */
  var rev = $$('.reveal');
  if ('IntersectionObserver' in window) {
    var io = new IntersectionObserver(function (es) {
      es.forEach(function (e) { if (e.isIntersecting) { e.target.classList.add('in'); io.unobserve(e.target); } });
    }, { threshold: .12, rootMargin: '0px 0px -40px 0px' });
    rev.forEach(function (el, i) { el.style.transitionDelay = (i % 4) * 70 + 'ms'; io.observe(el); });
  } else { rev.forEach(function (el) { el.classList.add('in'); }); }

  /* ---------- prompt box ---------- */
  var form = $('#askForm'), input = $('#askInput'), send = $('#sendBtn'), route = $('#route');
  var routeWeb = $('#routeWeb'), routeApp = $('#routeApp'), routeCopy = $('#routeCopy');
  var chipsEl = $('#chips');

  var LT = window.LT || {};
  var IDEAS = LT.ideas || { chat: [], code: [], create: [], think: [] };
  var PLACEHOLDERS = (LT.ph && LT.ph.length) ? LT.ph : [''];

  function renderChips(mode) {
    chipsEl.innerHTML = '';
    IDEAS[mode].forEach(function (t, i) {
      var b = document.createElement('button');
      b.type = 'button'; b.className = 'chip'; b.textContent = t; b.style.animationDelay = i * 60 + 'ms';
      b.addEventListener('click', function () { setPrompt(t); input.focus(); });
      chipsEl.appendChild(b);
    });
  }
  $$('.mode').forEach(function (m) {
    m.addEventListener('click', function () {
      $$('.mode').forEach(function (x) { x.classList.toggle('on', x === m); });
      renderChips(m.dataset.mode);
    });
  });
  renderChips('chat');

  function grow() { input.style.height = 'auto'; input.style.height = Math.min(input.scrollHeight, 180) + 'px'; }
  function setPrompt(t) { input.value = t; sync(); }
  function sync() { send.disabled = !input.value.trim(); grow(); if (!route.hidden) updateRoute(); }
  input.addEventListener('input', sync);

  // rotating placeholder while idle
  var phI = 0;
  if (!reduced) {
    setInterval(function () {
      if (document.activeElement === input || input.value) return;
      phI = (phI + 1) % PLACEHOLDERS.length; input.placeholder = PLACEHOLDERS[phI];
    }, 3200);
  }

  function promptText() { return input.value.trim(); }
  function updateRoute() {
    var p = promptText();
    // A URL fragment (#...) is never sent to any server, unlike a ?query: the prompt stays in the browser.
    routeWeb.href = WEB_APP + (p ? '#prompt=' + encodeURIComponent(p) : '');
  }
  function openRoute() {
    if (!promptText()) return;
    updateRoute(); route.hidden = false;
    route.scrollIntoView({ block: 'nearest', behavior: reduced ? 'auto' : 'smooth' });
    routeWeb.focus({ preventScroll: true });
  }
  function closeRoute() { route.hidden = true; input.focus(); }

  form.addEventListener('submit', function (e) { e.preventDefault(); openRoute(); });
  input.addEventListener('keydown', function (e) {
    if (e.key === 'Enter' && !e.shiftKey && !e.isComposing) { e.preventDefault(); openRoute(); }
  });
  $('#routeX').addEventListener('click', closeRoute);
  route.addEventListener('keydown', function (e) { if (e.key === 'Escape') closeRoute(); });
  routeWeb.addEventListener('click', function () { });
  routeApp.addEventListener('click', function () {
    copy(promptText()).then(function (ok) { toast(ok ? LT.toastApp : LT.toastPick); });
    route.hidden = true;
  });
  routeCopy.addEventListener('click', function () {
    copy(promptText()).then(function (ok) { toast(ok ? LT.toastCopied : LT.toastFailed); });
  });
  $('#ctaTop').addEventListener('click', function (e) {
    e.preventDefault();
    window.scrollTo({ top: 0, behavior: reduced ? 'auto' : 'smooth' });
    setTimeout(function () { input.focus({ preventScroll: true }); }, reduced ? 0 : 500);
  });

  /* ---------- GitHub: stars + latest desktop builds ---------- */
  var gh = window.LT_GH || {};
  function applyStars(n) {
    var el = $('#ghStars'); if (!el || !n) return;
    el.textContent = n >= 1000 ? (n / 1000).toFixed(1).replace('.0', '') + 'k' : n; el.hidden = false;
  }
  function applyRelease(assets) {
    $$('[data-ext]').forEach(function (a) {
      var ext = '.' + a.dataset.ext;
      var hit = assets.filter(function (x) { return x.name.toLowerCase().slice(-ext.length) === ext; })[0];
      if (hit) { a.href = hit.url; a.target = '_blank'; a.rel = 'noopener'; }
    });
  }
  /* Values are baked in at build time (see lt-head.html): no request to api.github.com from the browser. */
  if (gh.stars) applyStars(gh.stars);
  if (gh.assets) applyRelease(gh.assets);

  /* ---------- recommend the visitor's platform ---------- */
  (function () {
    var ua = navigator.userAgent || '', p = navigator.platform || '', os = 'web';
    if (/android/i.test(ua)) os = 'android';
    else if (/iphone|ipad|ipod/i.test(ua) || (p === 'MacIntel' && navigator.maxTouchPoints > 1)) os = 'ios';
    else if (/win/i.test(p)) os = 'win';
    else if (/mac/i.test(p)) os = 'mac';
    else if (/linux|x11/i.test(p + ua)) os = 'linux';
    var card = $('.dl-card[data-os="' + os + '"]');
    if (card && os !== 'web') {
      card.classList.add('rec');
      card.setAttribute('data-rec', LT.rec || '');
      card.parentNode.insertBefore(card, card.parentNode.firstChild);
    }
  })();

  /* ---------- typing demos ---------- */
  function typeLoop(el, text, opts) {
    opts = opts || {};
    if (!el) return;
    if (reduced) { el.textContent = text; return; }
    var i = 0, started = false;
    function tick() {
      i++; el.textContent = text.slice(0, i);
      if (i < text.length) setTimeout(tick, opts.speed || 28);
      else if (opts.loop) setTimeout(function () { i = 0; tick(); }, opts.pause || 4200);
    }
    var o = new IntersectionObserver(function (es) {
      if (es[0].isIntersecting && !started) { started = true; tick(); o.disconnect(); }
    }, { threshold: .4 });
    o.observe(el);
  }
  typeLoop($('#typeOut'), LT.typeA || '', { loop: true, speed: 24 });
  typeLoop($('#termOut'),
    'data: {"event":"delta","text":"Hi "}\ndata: {"event":"delta","text":"from "}\ndata: {"event":"delta","text":"your own server!"}\ndata: {"event":"done"}',
    { speed: 14, loop: true, pause: 6000 });

  /* ---------- code window ---------- */
  $$('.cw-tab').forEach(function (t) {
    t.addEventListener('click', function () {
      $$('.cw-tab').forEach(function (x) { x.classList.toggle('on', x === t); });
      $$('.cw-pane').forEach(function (p) { p.classList.toggle('on', p.dataset.pane === t.dataset.tab); });
    });
  });
  $('#cwCopy').addEventListener('click', function () {
    var pane = $('.cw-pane.on');
    copy(pane.textContent).then(function (ok) { toast(ok ? LT.toastCode : LT.toastFailed); });
  });
  $$('.mod').forEach(function (m) {
    m.addEventListener('click', function () {
      copy(m.dataset.copyText).then(function (ok) { toast(ok ? LT.toastModule.replace('{name}', m.firstElementChild.textContent) : LT.toastFailed); });
    });
  });

  /* ---------- deep link: #try focuses the prompt ---------- */
  if (location.hash === '#try') input.focus();
})();
