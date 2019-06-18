import React, { Component } from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom'
import ListTradesComponent from './ListTradesComponent';
import TradeComponent from './TradeComponent';
import Home from '../container/Home';
import Executions from './Executions';

class TradeOrderScreen extends Component {
    render() {
        return (
            <Router>
                <Switch>
                    <Route path="/" exact component={Home} />
                    <Route path="/trades" exact component={ListTradesComponent} />
                    <Route path="/trades/addNew" exact component={TradeComponent} />
                    <Route path="/executions" exact component={Executions} />
                </Switch>
            </Router>
        )
    }
}
export default TradeOrderScreen