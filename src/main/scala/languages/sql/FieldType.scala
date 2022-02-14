package com.jeekrs.unidef
package languages.sql

import utils.ExtKeyBoolean

object FieldType {
  case object PrimaryKey extends ExtKeyBoolean
  case object AutoIncr extends ExtKeyBoolean
  case object Nullable extends ExtKeyBoolean
}
