import React, {Component} from 'react'
import {Formik, Form, Field} from 'formik';
import axios from 'axios';
import TradeDataService from '../service/TradeDataService';

class TradeComponent extends Component {
    constructor(props) {
        super(props)
        this.state = { id: this.props.match.params.id,}
        this.onSubmit = this.onSubmit.bind(this)
    }

    onSubmit(values) {
        let trade = {
            quantity: values.quantity,
            side: values.side,
            ticker: values.ticker,
            orderType: values.orderType,
            orderStatus: values.orderStatus
        }

        axios({ method: 'post', url: TradeDataService.getAPIUrl()+"/trade", data: trade })
            .then(() => {
                setTimeout(function () {
                    window.location.href = "/";
                },500);
            })
    }

    render() {
        return (
            <div>
                <h3>Add Trade</h3>
                <div className="container">
                    <Formik
                        onSubmit={this.onSubmit}
                        validateOnChange={false}
                        validateOnBlur={false}
                        enableReinitialize={true}
                    >
                        {
                            (props) => (
                                <Form>
                                    <fieldset className="form-group">
                                        <label>Quantity</label>
                                        <Field className="form-control" type="text" name="quantity" required/>
                                    </fieldset>
                                    <fieldset className="form-group">
                                        <label>Side</label>
                                        <Field className="custom-select" component="select" name="side">
                                            <option value="BUY">BUY</option>
                                            <option value="SELL">SELL</option>
                                        </Field>
                                    </fieldset>
                                    <fieldset className="form-group">
                                        <label>Ticker</label>
                                        <Field className="form-control" type="text" name="ticker" required/>
                                    </fieldset>
                                    <fieldset className="form-group">
                                        <label>OrderType</label>
                                        <Field className="form-control" type="text" name="orderType" required/>
                                    </fieldset>
                                    <fieldset className="form-group">
                                        <label>Order Status</label>
                                        <Field className="form-control" component="select" name="orderStatus">
                                            <option value="NEW">NEW</option>
                                            <option value="STREAMING">STREAMING</option>
                                            <option value="COMPLETE">COMPLETE</option>
                                        </Field>
                                    </fieldset>
                                    <button className="btn btn-success" type="submit">Save</button>
                                </Form>
                            )
                        }
                    </Formik>
                </div>
            </div>
        )
    }
}

export default TradeComponent