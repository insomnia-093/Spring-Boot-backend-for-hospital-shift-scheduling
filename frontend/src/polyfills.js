// Minimal browser polyfills for libs that expect Node globals.
if (typeof globalThis.global === 'undefined') {
  globalThis.global = globalThis;
}

if (typeof globalThis.process === 'undefined') {
  globalThis.process = { env: {} };
}
