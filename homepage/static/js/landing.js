/* Llamatik landing page behaviour. No dependencies. */
(function () {
  'use strict';
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var $$ = function (s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); };
  var reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var WEB_APP = '/prompt/';
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

  var IDEAS = {
    chat: ['Explain quantum computing like I\'m five', 'Draft a polite email declining a meeting', 'Give me a 3-day itinerary for Lisbon', 'Summarize the pros and cons of local AI'],
    code: ['Write a Kotlin coroutine that retries with backoff', 'Explain this regex: ^(?=.*\\d)[A-Za-z\\d]{8,}$', 'Convert a Java stream pipeline to Kotlin Flow', 'Write unit tests for a shopping cart class'],
    create: ['A llama astronaut planting a flag on Mars, watercolor', 'Write a short story about a lighthouse keeper', 'Give me 10 names for a hiking app', 'Write a haiku about offline AI'],
    think: ['Plan a week of meals under 60 dollars', 'What are the trade-offs of microservices vs a monolith?', 'Help me prepare for a salary negotiation', 'Teach me how transformers work in 5 steps']
  };
  var PLACEHOLDERS = ['Ask anything…', 'Write a Kotlin function that…', 'Summarize this idea for me…', 'Help me plan my week…', 'Explain how this works…'];

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
    routeWeb.href = WEB_APP + (p ? '?prompt=' + encodeURIComponent(p) : '');
  }
  function openRoute() {
    if (!promptText()) return;
    updateRoute(); route.hidden = false;
    route.scrollIntoView({ block: 'nearest', behavior: reduced ? 'auto' : 'smooth' });
    routeWeb.focus({ preventScroll: true });
    if (window.gtag) gtag('event', 'hero_prompt_submit');
  }
  function closeRoute() { route.hidden = true; input.focus(); }

  form.addEventListener('submit', function (e) { e.preventDefault(); openRoute(); });
  input.addEventListener('keydown', function (e) {
    if (e.key === 'Enter' && !e.shiftKey && !e.isComposing) { e.preventDefault(); openRoute(); }
  });
  $('#routeX').addEventListener('click', closeRoute);
  route.addEventListener('keydown', function (e) { if (e.key === 'Escape') closeRoute(); });
  routeWeb.addEventListener('click', function () { if (window.gtag) gtag('event', 'hero_route_web'); });
  routeApp.addEventListener('click', function () {
    copy(promptText()).then(function (ok) { toast(ok ? 'Prompt copied — paste it in the app' : 'Pick your platform below'); });
    route.hidden = true;
    if (window.gtag) gtag('event', 'hero_route_app');
  });
  routeCopy.addEventListener('click', function () {
    copy(promptText()).then(function (ok) { toast(ok ? 'Prompt copied' : 'Copy failed'); });
  });
  $('#ctaTop').addEventListener('click', function (e) {
    e.preventDefault();
    window.scrollTo({ top: 0, behavior: reduced ? 'auto' : 'smooth' });
    setTimeout(function () { input.focus({ preventScroll: true }); }, reduced ? 0 : 500);
  });

  /* ---------- GitHub: stars + latest desktop builds ---------- */
  var cache = {};
  try { cache = JSON.parse(sessionStorage.getItem('lt_gh') || '{}'); } catch (e) {}
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
  if (cache.stars) applyStars(cache.stars);
  if (cache.assets) applyRelease(cache.assets);
  if (!cache.stars && window.fetch) {
    fetch('https://api.github.com/repos/' + REPO).then(function (r) { return r.ok ? r.json() : null; }).then(function (j) {
      if (j && j.stargazers_count) { cache.stars = j.stargazers_count; applyStars(cache.stars); save(); }
    }).catch(function () {});
    fetch('https://api.github.com/repos/' + REPO + '/releases/latest').then(function (r) { return r.ok ? r.json() : null; }).then(function (j) {
      if (j && j.assets) {
        cache.assets = j.assets.map(function (a) { return { name: a.name, url: a.browser_download_url }; });
        applyRelease(cache.assets); save();
      }
    }).catch(function () {});
  }
  function save() { try { sessionStorage.setItem('lt_gh', JSON.stringify(cache)); } catch (e) {} }

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
  typeLoop($('#typeOut'),
    'Imagine a coroutine as a toy that can pause and resume. It stops when it needs to wait — like for a snack — and picks up exactly where it left off, so nothing else has to stand still.',
    { loop: true, speed: 24 });
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
    copy(pane.textContent).then(function (ok) { toast(ok ? 'Code copied' : 'Copy failed'); });
  });
  $$('.mod').forEach(function (m) {
    m.addEventListener('click', function () {
      copy(m.dataset.copyText).then(function (ok) { toast(ok ? 'Copied: ' + m.firstElementChild.textContent : 'Copy failed'); });
    });
  });

  /* ---------- deep link: #try focuses the prompt ---------- */
  if (location.hash === '#try') input.focus();
})();
