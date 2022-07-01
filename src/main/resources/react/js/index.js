import "core-js/stable";
import "regenerator-runtime/runtime";
import React from 'react'
import { createRoot } from 'react-dom/client';
import { injectGlobal } from 'styled-components'

import App from 'App'

const root = createRoot(document.getElementById('react_select_container'));
root.render(<App />)

// Leaving this here, in case it's needed for something in the future
// eslint-disable-next-line
//injectGlobal`
//  body {
//  }
//`

if (module.hot) {
  module.hot.accept(App)
}
