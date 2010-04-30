//
// PythonLink.c
//

#include <stdlib.h>
#include <jni.h>
#include <Python.h>

#include "PythonLink.h"

JNIEXPORT void JNICALL Java_PythonLink_initialize
  (JNIEnv *env, jclass klass)
{
  Py_Initialize();
}

JNIEXPORT void JNICALL Java_PythonLink_runSimpleString
  (JNIEnv *env, jclass klass, jstring str)
{
  // convert jstring to char*
  const char* cStr = (*env)->GetStringUTFChars(env, str, 0);

  // call Python method
  PyRun_SimpleString(cStr);

  // release resources
  (*env)->ReleaseStringUTFChars(env, str, cStr);
}

JNIEXPORT void JNICALL Java_PythonLink_runString
  (JNIEnv *env, jclass klass, jstring command, jobject locals)
{
  char memory_addr[30];
  PyObject *py_jutil = NULL;
  PyObject *py_attach_ext_env = NULL;
  PyObject *py_make_run_dictionary = NULL;
  PyObject *py_env = NULL;
  PyObject *py_globals = NULL;
  PyObject *py_locals = NULL;
  PyObject *py_args = NULL;
  PyObject *py_result = NULL;
  PyObject *py_main = NULL;
  const char* cStr = NULL;
  const char* exception = NULL;
  jclass exception_class;

  py_jutil = PyImport_ImportModule("cellprofiler.utilities.jutil");
  if (py_jutil == NULL) {
    exception = "Failed to import cellprofiler.utilities.jutil";
    goto exit;
    }
  py_attach_ext_env = PyObject_GetAttrString(py_jutil, "attach_ext_env");
  if (py_attach_ext_env == NULL) {
    exception = "Failed to find the function, cellprofiler.utilities.jutil.attach_ext_env()";
    goto exit;
    }
  py_make_run_dictionary = PyObject_GetAttrString(py_jutil, "make_run_dictionary");
  if (py_make_run_dictionary == NULL) {
    exception = "Failed to find the function, cellprofiler.utilities.jutil.make_run_dictionary";
    goto exit;
  }
  //ltoa((long)env, memory_addr, 10);
  sprintf(memory_addr, "%p", env);
  py_args = Py_BuildValue("(s)", memory_addr);
  if (py_args == NULL) {
    exception = "Failed to build arguments for attach_ext_env";
    goto exit;
    }
  py_env = PyEval_CallObject(py_attach_ext_env, py_args);
  if (py_env == NULL) {
    exception = "Failed during call to attach_ext_env";
    goto exit;
    }
  Py_DECREF(py_args);

  py_main = PyImport_ImportModule("__main__");
  if (py_main == NULL) {
    exception = "Could not load __main__";
    goto exit;
    }
  py_globals = PyModule_GetDict(py_main);
  if (py_globals == NULL) {
    exception = "Couldn't get __main__ dict";
    goto exit;
    }
  //ltoa((long)locals, memory_addr, 10);
  sprintf(memory_addr, "%p", locals);
  py_args = Py_BuildValue("(s)", memory_addr);
  py_locals = PyEval_CallObject(py_make_run_dictionary, py_args);
  Py_DECREF(py_args);
  if (py_locals == NULL) {
    exception = "Failed to make locals dictionary";
    goto exit;
    }
  cStr = (*env)->GetStringUTFChars(env, command, 0);
  py_result = PyRun_String(cStr, Py_file_input, py_globals, py_locals);
  if (PyErr_Occurred()) {
    PyErr_Print();
    exception = "Failed to run script";
  }

  // release resources
  (*env)->ReleaseStringUTFChars(env, command, cStr);

exit:
  if (py_result)
    Py_DECREF(py_result);
  if (py_locals)
    Py_DECREF(py_locals);
  if (py_env)
    Py_DECREF(py_env);
  if (py_attach_ext_env)
    Py_DECREF(py_attach_ext_env);
  if (py_make_run_dictionary)
    Py_DECREF(py_make_run_dictionary);
  if (py_jutil)
    Py_DECREF(py_jutil);
  if (exception) {
    exception_class = (*env)->FindClass(env, "java/lang/Exception");
    (*env)->ThrowNew(env, exception_class, exception);
  }
}
