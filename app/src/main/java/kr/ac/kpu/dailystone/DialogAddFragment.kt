package kr.ac.kpu.dailystone

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.renderscript.Sampler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.dialog_happy.*
import java.lang.NumberFormatException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap


@RequiresApi(Build.VERSION_CODES.O)
class DialogAddFragment(context: Context) : Dialog(context) {
    private var mAuth: FirebaseAuth? = null
    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var db: DatabaseReference
    private lateinit var level : Any
    private lateinit var diary : Any
    private val current: LocalDate = LocalDate.now()
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val formatted: String = current.format(formatter)
    var date = formatted.substring(2,8)
    private val formatterYear: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy")
    private val yearformatted: String = current.format(formatterYear)
    var year = yearformatted.substring(2,4)
    private val formatterMonth : DateTimeFormatter = DateTimeFormatter.ofPattern("MM")
    private val monthformatted: String = current.format(formatterMonth)
    private val formatterDay : DateTimeFormatter = DateTimeFormatter.ofPattern("dd")
    private val dayformatted: String = current.format(formatterDay)
    var user: FirebaseUser?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_happy)

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().currentUser

        db=Firebase.database.reference
        var uid = user?.uid.toString()
        //readID(uid)


        dhBtnH1.setOnClickListener{
            dhEdHl.setText("30")
            level = dhEdHl.text.toString()
        }

        dhBtnH2.setOnClickListener{
            dhEdHl.setText("60")
            level = dhEdHl.text.toString()
        }

        dhBtnH3.setOnClickListener{
            dhEdHl.setText("100")
            level = dhEdHl.text.toString()
        }

        dhBtnDice.setOnClickListener {
            var rnd = Random()
            var num = rnd.nextInt(100)
            dhEdHl.setText(num.toString())
            level = dhEdHl.text.toString()

        }

        dhSbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    dhEdHl.setText(progress.toString());
                    level = dhEdHl.text.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        dhEdHl.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                try {
                    var i: String? = dhEdHl.text.toString()
                    if (i == "") {
                        i = "0"
                    }
                    var str: Int? = i?.let { Integer.parseInt(it) }

                    if ((i!!.toInt() > 100) || (i!!.toInt() < 0)) {
                        Toast.makeText(context, "0부터 100까지의 숫자만 입력해주세요", Toast.LENGTH_SHORT).show()
                        dhEdHl.setText("0")
                    } else {
                        dhSbar.progress = str!!
                    }
                }catch(e: NumberFormatException){
                    Toast.makeText(context, "숫자만 입력가능합니다", Toast.LENGTH_SHORT).show()
                    return
                }
                dhEdHl.setSelection(dhEdHl.length())
                    /*if (str != null) {

                        dhSbar.progress = i
                    }
                    if(str == null){
                        str = 0.toString()
                        i = str.toInt()
                        dhSbar.progress = i
                    }*/
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })


        dhBtnYes.setOnClickListener {
            // 데이터베이스에 저장
            onWriteDBPost()

            Toast.makeText(context,"저장 완료",Toast.LENGTH_SHORT)


            dismiss()
        }
        dhBtnNo.setOnClickListener {
            dismiss();
            Toast.makeText(context,"취소",Toast.LENGTH_SHORT)
        }
    }
    fun onWriteDBPost() {
        db = Firebase.database.reference
        var user = FirebaseAuth.getInstance().currentUser
        var cnt:Any = 0
        level = dhEdHl.text.toString()
        diary = dhEdDiary.text.toString()
        //val myRef = database.getReference("posts")
        //val myRef = database.getReference(user?.uid.toString())
        Log.d("Han", "$cnt")
        val postValues: HashMap<String, Any> = HashMap()

        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child("count").child(date).child("count").value==null){
                    cnt=0
                }
                else{
                    cnt = snapshot.child("count").child(date).child("count").value!!
                }

                Log.d("Han", "cnt: $cnt")
                val postCounts: HashMap<String, Any> = HashMap()
                val myRefCount = db.child(user!!.uid).child("count").child(date)
                postCounts["count"] = (cnt.toString().toInt()+1).toString()          //카운트 조건 추가
                myRefCount.setValue(postCounts)

                val myRefDiary = db.child(user!!.uid).child("diary").child(year).child(monthformatted).child(dayformatted)
                    .child((cnt.toString().toInt()+1).toString())
                postValues["level"] = level
                postValues["diary"] = diary
                myRefDiary.setValue(postValues)


            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }


        db.child(user!!.uid).addValueEventListener(postListener)






        Toast.makeText(context,"저장 완료",Toast.LENGTH_SHORT)
    }
    /*
    fun readID(uid:String): String {
        db = Firebase.database.reference
        var Did: String="1"
        db.child(uid).child(formatterYear.toString()).child(formatterMonth.toString()).runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                if((mutableData.child("Did").value as? Long)!=null){
                    Did= mutableData.child("Did").value as String
                }else{
                    Did="1"
                }

                return Transaction.success(mutableData);
            }

            override fun onComplete(
                databaseError: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                //Toast.makeText(applicationContext,"Transaction:onComplete:$databaseError",Toast.LENGTH_LONG).show()
            }
        })
        return Did
    }
*/


}