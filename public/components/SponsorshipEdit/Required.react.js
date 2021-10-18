import React from 'react';

export const Required = (props) => {
    const message = props.message || "* Required";
    return !props.fieldValue ? <span className="tag-edit__label sponsorship-validation">{message}</span> : null
};
