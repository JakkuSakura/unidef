package com.jeekrs.unidef
package languages.sql

import languages.common.KeywordBoolean

object FieldType {
  case object PrimaryKey extends KeywordBoolean
  case object AutoIncr extends KeywordBoolean
  case object Nullable extends KeywordBoolean
}
