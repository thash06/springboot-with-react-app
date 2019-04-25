import React, {Component} from 'react';
import './css/App.css';
import TradeOrderScreen from './components/TradeOrderScreen';

class App extends Component {

  render() {
        return (
           <div className="container">
               <TradeOrderScreen/>
           </div>
        );
    }
}

export default App;
