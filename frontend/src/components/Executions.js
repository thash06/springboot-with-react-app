import React, {Component} from 'react'
import axios from 'axios';
import ExecutionsTable from './ExecutionsTable';
import TableData from './TableData';

class Executions extends Component {
    constructor(props) {
        super(props)
    }

    render() {
        return (
            <ExecutionsTable TableData={TableData}/>
        )
    }
}

export default Executions