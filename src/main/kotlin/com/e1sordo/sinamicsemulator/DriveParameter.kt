package com.e1sordo.sinamicsemulator

import javax.persistence.*

@Entity
@Table(name="DRIVE_PARAMETER")
data class DriveParameter (

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long = -1,

        @Column
        var paramId: Int = 0,

        @Column
        var paramIndex: Int = 0,

        @Column
        var value: String = "",

        @Column
        var dataType: String = ""
)
