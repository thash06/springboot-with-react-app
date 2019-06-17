import React from 'react';
import AppBar from '@material-ui/core/AppBar'
import Toolbar from '@material-ui/core/Toolbar';
import TextField from '@material-ui/core/TextField';

    function AppHeader({headerInput,handleTFChange, submitBtnClick}) {
        return (  
            <div>
                <AppBar position='fixed' color="default">
                    <Toolbar style={{ marginRight:10,  justifyContent:"space-between" }}>
                        <TextField
                            id="order-name"
                            label="Find order for.."
                            // className={classes.textField}
                            value={headerInput}
                            onChange={handleTFChange}
                            margin="normal"
                        />
                        <button onClick={()=>submitBtnClick()} className='btn-order'>NEW ORDER</button> 
                    </Toolbar>
                </AppBar>            
            </div>
        );
}
 
export default AppHeader;