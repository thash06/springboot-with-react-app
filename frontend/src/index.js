import React from 'react';
import ReactDOM from 'react-dom';
import './css/index.css';
import App from './App';

import * as serviceWorker from './registerServiceWorker';

ReactDOM.render(<App />, document.getElementById('root'));
serviceWorker.unregister();
