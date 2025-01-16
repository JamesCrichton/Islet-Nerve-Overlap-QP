//James Crichton, University of Exeter, Groovy QuPath script for Ellie Cheung
//Script creates new "IsletNerve" annotations for the regions of segmented nerve which overlap segmented islets. 
//These new annotations are given a "Name" which corresponds to their parent islet ID 
//Input requires annotations for nerve (one object for all - "merge" if they're separate), and separate islet annotations

import qupath.lib.scripting.QP

//1. Find all Islet and Nerve objects

IsletAnnotations = QP.getAnnotationObjects().findAll{it.getPathClass() == getPathClass("Islet")}
NerveAnnotations = QP.getAnnotationObjects().findAll{it.getPathClass() == getPathClass("Nerve")}

//2. If "Nerve" annotations are separate (>1), merge them together. This will simplify the oberlap calculations
if(NerveAnnotations.size()!=1) {
    QP.selectObjectsByClassification("Nerve") //select all Nerve annotations
    QP.mergeSelectedAnnotations() //merge all selected annotations
    resetSelection() // deselect annotations
}

//3. Create new objects representing the overlapping islet and nerve annotations

NerveGeom = NerveAnnotations[0].getROI().getGeometry() //get the geometry of the single merged Nerve annotation. This format is needed for the "intersection" function

//Loop through the Islet annotations
IsletAnnotations.each{anno->
    currentGeom=anno.getROI().getGeometry()//convert objects to geometries for comparison to Nerve
    GeomID = anno.getID()
    IsletNerveOvelapGeom=currentGeom.intersection(NerveGeom) //Generate new geometry of the overlapping Islet/Nerve territory

    if(IsletNerveOvelapGeom.getArea()>0){ //If an overlapping region has been found, do this
    IsletNerveOvelapROI = GeometryTools.geometryToROI(IsletNerveOvelapGeom, ImagePlane.getDefaultPlane()) // convert geom to an ROI format
    IsletNerveObj = PathObjects.createAnnotationObject(IsletNerveOvelapROI, getPathClass("IsletNerve") ) // format the ROI as an obvject with a new class
    IsletNerveObj.setName(GeomID.toString()) // This uses te "Name" key as a placeholder to take the ID of the parent islet. **RENAME COLUMN ON EXPORT**
    addObject(IsletNerveObj) // Add this object to the project

    }}

QP.resolveHierarchy()  //Update the hierarchy (make sure everything knows what's inside what