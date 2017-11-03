import React from 'react';
import R from 'ramda';
import PillarSectionsList from './PillarSectionsList.react';
import AddSection from './AddSection.react';

export default class PillarSections extends React.Component {

    constructor(props) {
        super(props);
    }

    removeSection(section) {
        const removeSectionFn = R.reject(R.equals(section));

        const updatedPillar = Object.assign({}, this.props.pillar, {
            sectionIds: removeSectionFn(this.props.pillar.sectionIds)
        });

        this.props.updatePillar(updatedPillar);
    }

    addSection(section) {
        const addSectionFn = R.append(section);

        const updatedPillar = Object.assign({}, this.props.pillar, {
            sectionIds: addSectionFn(this.props.pillar.sectionIds)
        });

        this.props.updatePillar(updatedPillar);
    }

    getSectionNameFromPath(sectionPath) {
        if (this.props.sections) {
            const section = this.props.sections.find(s => s.path === sectionPath);
            return section ? section.name : sectionPath;
        } else return sectionPath;
    }

    renderSection(sectionPath) {
        const title = this.getSectionNameFromPath(sectionPath);

        return (
            <tr className="pillar-sections__item" key={sectionPath}>
                <td>{title}</td>
                <td>
                    <i className="i-delete" onClick={this.removeSection.bind(this, sectionPath)} />
                </td>
            </tr>
        );
    }

    renderAddSectionButton() {
        return <AddSection pillar={this.props.pillar} sections={this.props.sections} onAddSection={this.addSection.bind(this)} />
    }

    render() {
        return (
            <PillarSectionsList title="Sections" headers={["Path", ""]} actionButton={this.renderAddSectionButton()}>
                {this.props.pillar.sectionIds.map(this.renderSection, this)}
            </PillarSectionsList>
        )
    }
}
