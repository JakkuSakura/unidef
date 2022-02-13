package com.jeekrs.unidef
package languages.sql

import utils.ExtKey

object FieldType {

  object PrimaryKey extends ExtKey {

    override type V = Boolean
  }

  object AutoIncr extends ExtKey {
    override type V = Boolean

  }
}
