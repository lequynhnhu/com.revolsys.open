/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.gdal.gdal;

public class XMLNode {
  protected static long getCPtr(final XMLNode obj) {
    return obj == null ? 0 : obj.swigCPtr;
  }
  protected static long getCPtrAndDisown(final XMLNode obj) {
    if (obj != null)
    {
      obj.swigCMemOwn= false;
    }
    return getCPtr(obj);
  }

  public static XMLNode ParseXMLFile(final String pszFilename) {
    final long cPtr = gdalJNI.XMLNode_ParseXMLFile(pszFilename);
    return cPtr == 0 ? null : new XMLNode(cPtr, true);
  }

  private long swigCPtr;

  protected boolean swigCMemOwn;

  protected XMLNode(final long cPtr, final boolean cMemoryOwn) {
    if (cPtr == 0) {
      throw new RuntimeException();
    }
    this.swigCMemOwn = cMemoryOwn;
    this.swigCPtr = cPtr;
  }

  public XMLNode(final String pszString) {
    this(gdalJNI.new_XMLNode__SWIG_0(pszString), true);
  }

  public XMLNode(final XMLNodeType eType, final String pszText) {
    this(gdalJNI.new_XMLNode__SWIG_1(eType.swigValue(), pszText), true);
  }

  /* Ensure that the GC doesn't collect any parent instance set from Java */
  protected void addReference(final Object reference) {
  }

  public void AddXMLChild(final XMLNode psChild) {
    gdalJNI.XMLNode_AddXMLChild(this.swigCPtr, this, XMLNode.getCPtr(psChild), psChild);
  }


  public void AddXMLSibling(final XMLNode psNewSibling) {
    gdalJNI.XMLNode_AddXMLSibling(this.swigCPtr, this, XMLNode.getCPtr(psNewSibling), psNewSibling);
  }

  public XMLNode Clone() {
    final long cPtr = gdalJNI.XMLNode_Clone(this.swigCPtr, this);
    return cPtr == 0 ? null : new XMLNode(cPtr, true);
  }

  public synchronized void delete() {
    if (this.swigCPtr != 0) {
      if (this.swigCMemOwn) {
        this.swigCMemOwn = false;
        gdalJNI.delete_XMLNode(this.swigCPtr);
      }
      this.swigCPtr = 0;
    }
  }

  @Override
  public boolean equals(final Object obj) {
    boolean equal = false;
    if (obj instanceof XMLNode) {
      equal = ((XMLNode)obj).swigCPtr == this.swigCPtr;
    }
    return equal;
  }

  @Override
  protected void finalize() {
    delete();
  }

  public XMLNode getChild() {
    final long cPtr = gdalJNI.XMLNode_Child_get(this.swigCPtr, this);
    return cPtr == 0 ? null : new XMLNode(cPtr, false);
  }

  public XMLNode getNext() {
    final long cPtr = gdalJNI.XMLNode_Next_get(this.swigCPtr, this);
    return cPtr == 0 ? null : new XMLNode(cPtr, false);
  }

  public XMLNodeType getType() {
    return XMLNodeType.swigToEnum(gdalJNI.XMLNode_Type_get(this.swigCPtr, this));
  }

  public String getValue() {
    return gdalJNI.XMLNode_Value_get(this.swigCPtr, this);
  }

  public XMLNode GetXMLNode(final String pszPath) {
    final long cPtr = gdalJNI.XMLNode_GetXMLNode(this.swigCPtr, this, pszPath);
    XMLNode ret = null;
    if (cPtr != 0) {
      ret = new XMLNode(cPtr, false);
      ret.addReference(this);
    }
    return ret;
  }

  public String GetXMLValue(final String pszPath, final String pszDefault) {
    return gdalJNI.XMLNode_GetXMLValue(this.swigCPtr, this, pszPath, pszDefault);
  }

  @Override
  public int hashCode() {
    return (int)this.swigCPtr;
  }

  public XMLNode SearchXMLNode(final String pszElement) {
    final long cPtr = gdalJNI.XMLNode_SearchXMLNode(this.swigCPtr, this, pszElement);
    XMLNode ret = null;
    if (cPtr != 0) {
      ret = new XMLNode(cPtr, false);
      ret.addReference(this);
    }
    return ret;
  }

  public String SerializeXMLTree() {
    return gdalJNI.XMLNode_SerializeXMLTree(this.swigCPtr, this);
  }

  public int SetXMLValue(final String pszPath, final String pszValue) {
    return gdalJNI.XMLNode_SetXMLValue(this.swigCPtr, this, pszPath, pszValue);
  }

  public void StripXMLNamespace(final String pszNamespace, final int bRecurse) {
    gdalJNI.XMLNode_StripXMLNamespace(this.swigCPtr, this, pszNamespace, bRecurse);
  }

  @Override
  public String toString() {
    return gdalJNI.XMLNode_toString(this.swigCPtr, this);
  }

}
