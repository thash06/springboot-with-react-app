import React, { Component } from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom'
import ListTradesComponent from './ListTradesComponent';
import TradeComponent from './TradeComponent';

class TradeOrderScreen extends Component {
    render() {
        return (
            <Router>
                <h1>Trade Order Screen Application</h1>
                <Switch>
                    <Route path="/" exact component={ListTradesComponent} />
                    <Route path="/trades" exact component={ListTradesComponent} />
                    <Route path="/trades/addNew" exact component={TradeComponent} />
                </Switch>
            </Router>
        )
    }
}
export default TradeOrderScreen