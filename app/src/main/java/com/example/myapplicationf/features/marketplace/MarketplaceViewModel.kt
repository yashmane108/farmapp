package com.example.myapplicationf.features.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationf.auth.AuthHelper
import com.example.myapplicationf.features.marketplace.models.BuyerDetail
import com.example.myapplicationf.features.marketplace.models.Category
import com.example.myapplicationf.features.marketplace.models.ListedCrop
import com.example.myapplicationf.features.marketplace.models.PurchaseRequest
import com.example.myapplicationf.features.marketplace.models.AcceptedRequest
import com.example.myapplicationf.features.marketplace.api.AgriPriceService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Date
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.firestore.Query
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth

data class CropItem(
    val name: String,
    val quantity: Int,
    val rate: Int,
    val location: String,
    val category: Category
)

// Data class to represent a taluka and its villages
data class Taluka(
    val name: String,
    val villages: List<String>
)

class MarketplaceViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val functions: FirebaseFunctions = Firebase.functions
    private val auth: FirebaseAuth = Firebase.auth
    private val cropsCollection = db.collection("crops")
    private var cropsListener: ListenerRegistration? = null
    private var myCropsListener: ListenerRegistration? = null
    private var purchaseRequestsListener: ListenerRegistration? = null

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _selectedProduct = MutableStateFlow<ListedCrop?>(null)
    val selectedProduct: StateFlow<ListedCrop?> = _selectedProduct.asStateFlow()

    private val _crops = MutableStateFlow<List<ListedCrop>>(emptyList())
    val crops: StateFlow<List<ListedCrop>> = _crops.asStateFlow()

    private val _foodsByCategory = MutableStateFlow<Map<Category, List<String>>>(emptyMap())
    val foodsByCategory: StateFlow<Map<Category, List<String>>> = _foodsByCategory.asStateFlow()

    private val _listedCrops = MutableStateFlow<List<ListedCrop>>(emptyList())
    val listedCrops: StateFlow<List<ListedCrop>> = _listedCrops.asStateFlow()

    private val _myListedCrops = MutableStateFlow<List<ListedCrop>>(emptyList())
    val myListedCrops: StateFlow<List<ListedCrop>> = _myListedCrops.asStateFlow()

    private val _purchaseRequests = MutableStateFlow<List<PurchaseRequest>>(emptyList())
    val purchaseRequests: StateFlow<List<PurchaseRequest>> = _purchaseRequests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _customRate = MutableStateFlow("")
    val customRate: StateFlow<String> = _customRate.asStateFlow()

    // Taluka and village related state
    private val _talukas = MutableStateFlow<List<Taluka>>(emptyList())
    val talukas: StateFlow<List<Taluka>> = _talukas.asStateFlow()

    private val _selectedTaluka = MutableStateFlow<String?>(null)
    val selectedTaluka: StateFlow<String?> = _selectedTaluka.asStateFlow()

    private val _villages = MutableStateFlow<List<String>>(emptyList())
    val villages: StateFlow<List<String>> = _villages.asStateFlow()

    private val _filteredVillages = MutableStateFlow<List<String>>(emptyList())
    val filteredVillages: StateFlow<List<String>> = _filteredVillages.asStateFlow()

    // Selected crop for detail view
    private val _selectedCrop = MutableStateFlow<ListedCrop?>(null)
    val selectedCrop: StateFlow<ListedCrop?> = _selectedCrop.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Add state for accepted requests
    private val _acceptedRequests = MutableStateFlow<List<AcceptedRequest>>(emptyList())
    val acceptedRequests: StateFlow<List<AcceptedRequest>> = _acceptedRequests.asStateFlow()

    private var acceptedRequestsListener: ListenerRegistration? = null

    // State for seller's purchase requests
    private val _sellerPurchaseRequests = MutableStateFlow<List<PurchaseRequest>>(emptyList())
    val sellerPurchaseRequests: StateFlow<List<PurchaseRequest>> = _sellerPurchaseRequests.asStateFlow()

    // Add these properties at the top of the MarketplaceViewModel class
    private val _todaysRate = MutableStateFlow<Double?>(null)
    val todaysRate: StateFlow<Double?> = _todaysRate.asStateFlow()

    private val _isLoadingPrice = MutableStateFlow(false)
    val isLoadingPrice: StateFlow<Boolean> = _isLoadingPrice.asStateFlow()

    private fun setupSellerPurchaseRequestsListener() {
        viewModelScope.launch {
            try {
                println("Debug: Setting up seller purchase requests listener")
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    println("Debug: No authenticated user found")
                    _error.value = "User not authenticated"
                    return@launch
                }

                // Listen for purchase requests where user is the seller
                db.collection("purchase_requests")
                    .whereEqualTo("sellerEmail", currentUser.email)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            println("Debug: Error listening to seller purchase requests: ${error.message}")
                            _error.value = "Error fetching seller purchase requests: ${error.message}"
                            return@addSnapshotListener
                        }

                        println("Debug: Received seller purchase requests snapshot. Documents count: ${snapshot?.documents?.size ?: 0}")
                        
                        val requests = snapshot?.documents?.mapNotNull { doc ->
                            try {
                                println("Debug: Processing seller purchase request document: ${doc.id}")
                                println("Debug: Document data: ${doc.data}")
                                PurchaseRequest.fromMap(doc.id, doc.data ?: emptyMap())
                            } catch (e: Exception) {
                                println("Debug: Error parsing seller purchase request: ${e.message}")
                                _error.value = "Error parsing seller purchase request: ${e.message}"
                                null
                            }
                        } ?: emptyList()
                        
                        println("Debug: Processed ${requests.size} valid seller purchase requests")
                        _sellerPurchaseRequests.value = requests
                    }
            } catch (e: Exception) {
                println("Debug: Error in setupSellerPurchaseRequestsListener: ${e.message}")
                e.printStackTrace()
                _error.value = "Error setting up seller purchase requests listener: ${e.message}"
            }
        }
    }

    init {
        initializeFoodsByCategory()
        initializeSataraTalukasAndVillages()
        setupRealTimeListeners()
        setupPurchaseRequestsListener()
        setupSellerPurchaseRequestsListener()
    }

    private fun initializeSataraTalukasAndVillages() {
        // Initialize with talukas and their villages from Satara district
        val sataraTalukas = listOf(
            Taluka("Jaoli", listOf(
                "Adoshi", "Agalavewadi", "Akhade", "Akhegani", "Alewadi", "Ambeghar T Medha", "Ambeghar T. Kudal", "Andhari", "Anewadi", "Apti", "Arde", "Asani", "Bahule", "Bamnoli T. Kudal", "Belawade", "Beloshi", "Bhaleghar", "Bhamghar", "Bhanang", "Bhivadi", "Bhogavali T. Medha", "Bhogavali T. Kudal", "Bhuteghar", "Bibhvi", "Bondarwadi", "Chorambe", "Dabhe Turuk", "Dangreghar", "Dapawadi", "Dare Bk. ", "Dare Kh. ", "Deur", "Dhanakwadi", "Dhondewadi", "Divdev", "Divdevwadi", "Duduskarwadi", "Dund", "Furus", "Galdev", "Ganje", "Gavdi", "Ghoteghar", "Gondemal", "Hateghar", "Humgaon", "Indavli", "Jambruk", "Jarewadi", "Jawalwadi", "Jungti", "Kaloshi", "Karandi T. Medha", "Karandoshi", "Karanje", "Karedi T. Kudal", "Kargaon", "Karhar", "Kas", "Kasbe Bamnoli", "Katavali", "Kavadi", "Kedambe", "Kelghar T Medha", "Kelghar T. Solshi", "Kenjal", "Kesakarwadi", "Kharshi Baramure", "Kharshi T. Kudal", "Khirkhandi", "Kolewadi", "Kolghar", "Kudal", "Kumbhargani", "Kuraloshi", "Kusapur", "Kusawade", "Kusumbi", "Madoshi", "Mahigaon", "Mahu", "Majare Shevandi", "Malchoundi", "Maldev", "Maleshwar", "Mamurdi", "Manti", "Maradmure", "Marli", "Mauje Shevandi", "Met Indawli", "Met Shindi", "Mhamulkarwadi", "Mhasve", "Mhate Bk", "Mhate Kh. ", "Mhavshi", "Mohat", "Morawale", "Morghar", "Mukavale", "Munawale", "Nandgane", "Narfdev", "Nipani", "Nizare", "Okhavadi", "Ozare", "Pali T. Tamb", "Panas", "Pawarwadi", "Phalani", "Pimpali", "Pimpri T. Medha", "Prabhuchiwadi", "Punavadi", "Ramwadi", "Rangeghar", "Ranjni", "Ravandi", "Raygaon", "Rendimura", "Rengadiwadi", "Ritkawali", "Ruighar", "Sahyadrinagar", "Sanapane", "Sangvi T. Kudal", "Sangvi T. Medha", "Sarjapur", "Sartale", "Savali", "Sawari", "Sawrat", "Sayali", "Saygaon", "Sayghar", "Shete", "Shindewadi", "Somardi", "Songaon", "Takawli", "Taloshi", "Tambi", "Tambi T. Medha", "Tetli", "Umbarewadi", "Vadgare", "Vagheshwar", "Vahagaon", "Vahite", "Valanjwadi", "Valuth", "Varoshi", "Vasota", "Vatambe", "Vele", "Vivar", "Waghali", "Waki", "Yekiv"

            )),
            Taluka("Karad", listOf(
                "Abachiwadi", "Akaichiwadi", "Ambavade", "Andharwadi", "Ane", "Antvadi", "Arewadi", "Atake", "Babarmachi", "Bamanwadi", "Bandakerwadi", "Banugadewadi", "Belavade Bk", "Beldari", "Belvade Haveli", "Belwadi", "Bhagatwadi", "Bharewadi", "Bhavanwadi", "Bholewadi", "Bhosalewadi", "Bhurbhushi", "Bhuyachiwadi", "Botrewadi", "British - Shirwade", "Chachegaon", "Chapnemala", "Charegaon", "Chaugulemala", "Chikhali", "Chinchani", "Chorajwadi", "Chore", "Chormarwadi", "Daphalwadi", "Delewadi", "Dhanakwadi", "Dhavarwadi", "Dhondewadi", "Dushere", "Gaikwadwadi", "Gamewadi", "Ganeshwadi", "Gharalwadi", "Gharewadi", "Ghogaon", "Gholapwadi", "Ghonasi", "Godwadi", "Goleshwar", "Gondi", "Gopalnagar", "Gosavewadi", "Gote", "Gotewadi", "Govare", "Hanbarwadi", "Hanumantwadi", "Hanumanwadi", "Harpalwadi", "Havelwadi", "Helgaon", "Hingnole", "Indoli", "Jadhavmala", "Jakhinwadi", "Jinti", "Julewadi", "Junjarwadi", "Kacharewadi", "Kalantrewadi", "Kale", "Kaletake", "Kalgaon", "Kalvade", "Kamathi", "Kambirwadi", "Kapil", "Karavadi", "Karve", "Kasarshirambe", "Kavathe", "Kese", "Khalkarwadi", "Kharade", "Khodjaiwadi", "Khodshi", "Khubi", "Kirpe", "Kival", "Kodoli", "Kole", "Kolewadi", "Konegaon", "Koparde Haveli", "Koregaon", "Koriwale", "Korti", "Koyanavasahat", "Kusur", "Latakewadi", "Loharwadi", "Maharugadewadi", "Malkhed", "Malwadi", "Mangwadi", "Manu", "Marali", "Maskarwadi", "Masur", "Matekerwadi", "Mervawadi", "Mhasoli", "Mhopre", "Munawale", "Mundhe", "Nadashi", "Nandgaon", "Nandlapur", "Narayanwadi", "Navin Gaonthun Gharewadi", "Navin Kavathe", "Navin Nandgaon", "Navinmalkheda", "Nigadi", "Ond", "Ondosha", "Pachund", "Pachupatewadi", "Pachwadwasahat", "Padali", "Padalikese", "Pal", "Parale", "Parle", "Pashchimsupane", "Patharwadi", "Patilmala", "Pawarwadi", "Pawarwadi", "Pimpari", "Potale", "Rajmachi", "Rethare Bk. ", "Rethare Kh. ", "Risvad", "Sabalwadi", "Sajur", "Sakurdi", "Salshirambe", "Sanjaynagar", "Sanjaynagar", "Savade", "Sayapur", "Shahapur", "Shakerwadi", "Shamgaon", "Shelakewadi", "Shelekewadi", "Shendawadi", "Shenoli", "Shenolistalion", "Shere", "Shevalwadi", "Shevalwadi", "Shevalwadi", "Shewalewadi", "Shindewadi", "Shinganwadi", "Shiravade", "Shirgaon", "Shitalwadi", "Shiwade", "Supane", "Surli", "Talbid", "Talgaon", "Tambave", "Tarukh", "Tasavade", "Tembhu", "Thoerathmal", "Thoratmala", "Tulsan", "Umbraj", "Undale", "Uttar Tambave", "Vadgaon Umbraj", "Vadoli Bhikeshwar", "Vadoli Nileshwar", "Vagheri", "Vahagaon", "Vanarwadi", "Vanavasmachi", "Vanvadi (Sadashivgad)", "Vanvasmachi", "Varade", "Varunji", "Vasantgad", "Vastisakurdi", "Vathar", "Vijaynagar", "Ving", "Virvade", "Vithobawadi", "Wadgaon Haveli", "Wagheshwar", "Wanyachiwadi", "Wazewadi", "Yadavawadi", "Yadavwadi", "Yelgaon", "Yenake", "Yenape", "Yeravale", "Yeshwantnagar", "Yevati"

            )),
            Taluka("Khandala", listOf(
                "Ahire", "Ajnuj", "Ambarwadi", "Andori", "Asawali", "Atit", "Balu Patlachiwadi", "Bavda", "Bavkalwadi", "Bhadavade", "Bhade", "Bhatghar", "Bholi", "Bori", "Dhangarwadi", "Dhawadwadi", "Ghadgewadi", "Ghatdare", "Guthalwadi", "Harali", "Hartali", "Javale", "Kanhavadi", "Kanheri", "Karadwadi", "Karnavadi", "Kavathe", "Kesurdi", "Khandala", "Khed Bk. ", "Koparde", "Limachiwadi", "Lohom", "Loni", "Mane Colony", "Mariachiwadi", "Mhavashi", "Mirje", "Moh Tarf Shirwal", "Morve", "Naigaon", "Nimbodi", "Padali", "Padegaon", "Palashi", "Pargaon", "Pimpare Bk. ", "Pisalwadi", "Rajewadi", "Rui", "Sangvi", "Shedgewadi", "Shekhmirwadi", "Shindewadi", "Shirwal", "Shivajinagar", "Sukhed", "Tondal", "Vadgaon", "Wadwadi", "Waghoshi", "Wanyachiwadi", "Wathar Bk. ", "Wing", "Yelewadi", "Zagalwadi"

            )),
            Taluka("Khatav", listOf(
                "Amalewadi", "Ambavade", "Ambheri", "Anpatwadi", "Anphale", "Aundh", "Banpuri", "Bhandewadi", "Bhosare", "Bhurakvadi", "Bhushangad", "Bitalewadi", "Bombale", "Budh", "Chinchani", "Chitali", "Chorade", "Dalmodi", "Dambhewadi", "Darajai", "Daruj", "Datewadi", "Dhakarwadi", "Dharpudi", "Dhokalwadi", "Dhondewadi", "Diskal", "Enkul", "Fadtarwadi", "Fadtarwadi Bk", "Gadewadi", "Ganeshwadi", "Ganeshwadi", "Garalewadi", "Garavadi", "Garudi", "Girijashankarwadi", "Gopuj", "Goregaon (N) (Vangi)", "Goregaon (Vangi)", "Gosavyachiwadi", "Gundewadi", "Gursale", "Hingane", "Hivarwadi", "Holichagaon", "Husenpur", "Jaigaon", "Jakhangaon", "Jamb", "Kalambi", "Kaledhon", "Kalewadi", "Kamathi T. Parali (N. V. )", "Kanharwadi", "Kankatre", "Kansewadi", "Karandewadi", "Katalgewadi", "Katarkhatav", "Katewadi", "Katgun", "Khabalwadi", "Kharashinge", "Khatav", "Khatgun", "Khatval", "Kokarale", "Kumathe", "Kurle", "Kuroli", "Ladegaon", "Lalgun", "Landewadi", "Laxminagar", "Loni", "Mandave", "Manetupewadi", "Manjarwadi", "Maradwak", "Mayani", "Mhasurne", "Mol", "Morale", "Mulikwadi", "Musandewadi", "Nagnathawadi", "Naikachiwadi", "Nandoshi", "Nathaval", "Navalewadi", "Ner", "Nidhal", "Nimsod", "Pachwad", "Padal", "Palasgaon", "Palashi", "Pandharwadi", "Pangarkhel", "Pargaon", "Pawarwadi", "Pedgaon", "Pimpari", "Pophalkarwadi (N. V. )", "Pusegaon", "Pusesawali", "Rahatani", "Rajapur", "Rameshwar", "Ramoshiwadi", "Ranshingwadi", "Revalkarwadi", "Satewadi", "Shenawadi", "Shendgewadi", "Shindewadi", "Shirsawadi", "Sundarpur", "Suryachiwadi", "Tadavale", "Taraswadi", "Thoravewadi", "Trimali", "Umbarde", "Umbarmale", "Unchithane", "Vadgaon (J. S. )", "Vadi", "Vadkhal", "Vaduj", "Vanzoli", "Vardhangad", "Varud", "Vetane", "Vikhale", "Visapur", "Wakalwadi", "Wakeshwar", "Yaralwadi", "Yeliv", "Yelmarwadi"

            )),
            Taluka("Koregaon", listOf(
                "Ambawade S. Koregaon", "Ambawade S. Wagholi", "Ambheri", "Anbhulwadi", "Anpatwadi", "Apshinge", "Arabwadi", "Arvi", "Asangaon", "Asgaon", "Bagewadi", "Banawadi", "Belewadi", "Bhadale", "Bhakarwadi", "Bhaktawadi", "Bhandarmachi", "Bhatamwadi", "Bhavenagar", "Bhimnagar", "Bhivadi", "Bhose", "Bichukale", "Bobadewadi", "Bodhewadi", "Bodhewadi", "Borgaon", "Borjaiwadi", "Chadvadi", "Chanchali", "Chaudharwadi", "Chawaneshwar", "Chilewadi", "Chimangaon", "Dahigaon", "Dare T. Tamb", "Deur", "Dhamner", "Dhumalwadi", "Dudhanwadi", "Dudhi", "Durgalwadi", "Ekambe", "Eksal", "Fadtarwadi", "Ghigewadi", "Godsewadi", "Gogavalewadi", "Golewadi", "Gujarwadi", "Gujarwadi", "Hasewadi", "Hivare", "Holewadi", "Jadhavwadi", "Jagtapwadi", "Jaigaon", "Jalgaon", "Jamb Bk. ", "Jamb Kh. ", "Jarewadi", "Kaloshi", "Kanherkhed", "Karanjkhop", "Katewadi", "Kathapur", "Kawadewadi", "Khadkhadwadi", "Khamakarwadi", "Khed", "Khirkhindi", "Kinhai", "Kiroli", "Kolavadi", "Kombadwadi", "Kumathe", "Lhasurne", "Madanapurwadi", "Mangalapur", "Mohitewadi", "Moreband", "Mugaon", "Nagewadi", "Nagzari", "Nalavadewadi", "Nalavadewadi", "Nandwal", "Naygaon", "Nhavi Bk", "Nhavi Kh. ", "Nigadi", "Palashi", "Paratwadi", "Pawarwadi", "Pimpode Bk. ", "Pimpode Kh. ", "Pimpri", "Ramoshiwadi", "Randullabad", "Rautwadi", "Revdi", "Rikibadarwadi", "Rui", "Saigaon", "Saigaon", "Sangavi", "Sap", "Sarkalwadi", "Sasurve", "Satara Road", "Sathewadi", "Shahapur", "Shelti", "Shendurjane", "Shirambe", "Shirdhon", "Siddharthnagar", "Solashi", "Sonake", "Sultanwadi", "Surli", "Tadawale S. Wagholi", "Tadawale S. Koregaon", "Takale", "Talaye", "Tambi", "Tandulwadi", "Targaon.", "Triputi", "Vadachiwadi", "Velang", "Velang", "Velu", "Vikhale", "Waghajaiwada", "Wagholi", "Wathar Kiroli", "Wathar Station"

            )),
            Taluka("Mahabaleshwar", listOf(
                "Achali", "Adhal", "Ahire", "Akalpe", "Ambral", "Amshi", "Araw", "Avakali", "Awalan", "Bhekavali", "Bhilar", "Bhose", "Birmāni", "Birwadi", "Chakdev", "Chaturbet", "Chikhali", "Dabhe Dabhekar", "Dabhe Mohan", "Dandeghar", "Danvali", "Dare", "Dare Tamb", "Devali", "Devasare", "Dhangarwadi", "Dhardev", "Dhavali", "Dodani", "Dudhgaon", "Dudhoshi", "Gadhavali", "Gavadhoshi", "Ghavari", "Ghonaspur", "Godavali", "Gogave", "Goroshi", "Gureghar", "Harchandi", "Haroshi", "Hatlot", "Jaoli", "Kalamgaon", "Kalamgaon Kalamkar", "Kandat", "Kasrud", "Kaswand", "Khambil Chorge", "Khambil Pokale", "Kharoshi", "Khengar", "Kotroshi", "Kshetra Mahabaleshwar", "Kumbharoshi", "Kumthe", "Kuroshi", "Lakhwad", "Lamaj", "Machutar", "Majarewadi", "Malusar", "Manghar", "Met Taliye", "Metgutad", "Mhalunge", "Moleshwar", "Morni", "Nakinda", "Navali", "Nivali", "Pali T. Ategaon", "Pangari", "Parpar", "Parsond", "Parwat T. Wagawale", "Petpar", "Pimpri T. Tamb", "Rajapur", "Rameghar", "Ran Adva Gaund", "Renoshi", "Rule", "Saloshi", "Shindi", "Shindola", "Shiravali", "Shirnar", "Sonat", "Soundari", "Taighat", "Taldev", "Tapola", "Tekavali", "Uchat", "Umbari", "Valawan", "Vanavli T. Ategaon", "Vanavli T. Solasi", "Varsoli Dev", "Varsoli Koli", "Velapur", "Vengale", "Vivar", "Walne", "Yerandal", "Yerne Bk", "Yerne Kh", "Zadani", "Zanzwad"

            )),
            Taluka("Man", listOf(
                "Agaswadi", "Andhali", "Anubhulewadi", "Bangarwadi", "Bhalavadi", "Bhandavali", "Bhatki", "Bidal", "Bijavadi", "Bodake", "Bothe", "Chillarwadi", "Dahivadi", "Danavalewadi", "Dangirewadi", "Dangirewadi", "Devapur", "Dhakani", "Dhamani", "Dhuldev", "Didwaghwadi (Divad)", "Divad", "Divadi (Mahimangad)", "Dorgewadi (Naravane)", "Gadewadi", "Gangoti", "Garadachiwadi (Varugad)", "Gatewadi", "Gherewadi", "Gondavale Bk.", "Gondavale Kh.", "Hawaldarwadi (Paryanti)", "Hingani", "Injabav", "Jadhavwadi", "Jambhulani", "Jashi", "Kalaskarwadi (Kulakjai)", "Kalchondi", "Kalewadi (Naravane)", "Karkhel", "Kasarwadi (Andhali)", "Khadaki", "Khandyachiwadi(Varugad)", "Khokade", "Khokade", "Khutbav", "Kiraksal", "Kolewadi", "Kukudwad", "Kulakjai", "Kuranwadi", "Lodhavade", "Mahabaleshwar Wadi", "Mahimangad", "Malavadi", "Mankarnawadi", "Mardi", "Mogarale", "Mohi", "Naravane", "Pachvad", "Palashi", "Palsavade", "Panavan", "Pandharwadi (Mahimangad)", "Pangari", "Parkhandi", "Paryanti", "Pimpari", "Pingali Bk", "Pingali Kh.", "Pukalewadi", "Pulkoti", "Rajavadi", "Ranand", "Ranjani", "Sambhukhed", "Satrewadi (Malavadi)", "Shenwadi", "Shevari", "Shindi Bk.", "Shindi Kh", "Shinganapur", "Shiravali", "Shirtav", "Shripanvan", "Sokasan", "Swarupkhanwadi (Mahimangad)", "Takewadi (Andhali)", "Thadale", "Tondale", "Ugalyachiwadi (Varugad)", "Ukirde", "Vadgaon", "Valai", "Varkute Malavadi", "Varugad", "Virali", "Virobanagar", "Wadjal", "Waki", "Warkute Mhaswad", "Wawarhire", "Yelegaon"

            )),
            Taluka("Patan", listOf(
                "Aadev Kh.", "Abdarwadi", "Acharewadi", "Adadev", "Adul", "Ambale", "Ambavade Kh.", "Ambavane", "Ambeghar Tarf Marli", "Ambewadi", "Ambrag", "Ambrule", "Aral", "Asawalewadi", "Atoli", "Awarde", "Bacholi", "Bagalwadi", "Bahe", "Bahule", "Baje", "Bamanewadi", "Bamanwadi", "Bambavade", "Bandhvat", "Banpethwadi", "Banpuri", "Belavade Kh.", "Bhambe", "Bharewadi", "Bharsakhale", "Bhilarwadi", "Bhosgaon", "Bhudakewadi", "Bibi", "Bodakewadi", "Bondri", "Bopoli", "Boragewadi", "Borgewadi", "Borgewadi", "Borgewadi", "Chafal", "Chafer", "Chafoli", "Chalkewadi", "Chavanwadi", "Chavanwadi (Dhamani)", "Chawaliwadi", "Chikhalewadi", "Chiteghar", "Chopadi", "Chopdarwadi", "Chougulewadi", "Chougulewadi", "Dadholi", "Dakewadi (Kalgaon)", "Dakewadi (Wazoli)", "Dangistewadi", "Daphalwadi", "Davari", "Dervan", "Deshmukhwadi", "Devghar Tarf Patan", "Dhadamwadi", "Dhajgaon", "Dhamani", "Dhangarwadi", "Dhavade", "Dhayati", "Dhebewadi", "Dhokawale", "Dhoroshi", "Dhuilwadi", "Dicholi", "Digewadi", "Dikshi", "Divashi Bk.", "Divashi Kh", "Dongarobachiwadi", "Donglewadi", "Donichawada", "Dusale", "Dutalwadi", "Ekavadewadi", "Fartarwadi", "Gadhav Khop", "Galmewadi", "Gamewadi", "Garawade", "Gavhanwadi", "Gawalinagar", "Gawdewadi", "Gaymukhwadi", "Ghanav", "Ghanbi", "Ghatewadi", "Ghatmatha", "Gheradategad", "Ghot", "Ghotil", "Giraswadi", "Girewadi", "Gokul Tarf Helwak", "Gokul Tarf Patan", "Gorewadi", "Goshatwadi", "Gothane", "Govare", "Gudhe", "Gujarwadi", "Gunjali", "Guteghar", "Harugdewadi", "Helwak", "Humbarli", "Humbarne", "Humbarwadi", "Jadhavwadi", "Jadhavwadi", "Jaichiwadi", "Jalagewadi", "Jalu", "Jambhalwadi", "Jambhali", "Jangal Wadi", "Janugdewadi", "Jarewadi", "Jinti", "Jugaiwadi", "Jungati", "Jyotibachiwadi", "Kadave Bk.", "Kadave Kh", "Kadhane", "Kadoli", "Kahir", "Kalambe", "Kalgaon", "Kalkewadi", "Kaloli", "Kamargaon", "Karale", "Karapewadi", "Karate", "Karpewadi", "Karvat", "Kasani", "Kasrund", "Katewadi", "Kathi", "Katvadi", "Kavadewadi", "Kavarwadi", "Keloli", "Kemase", "Ker", "Keral", "Khale", "Kharadwadi", "Khilarwadi", "Khivashi", "Khonoli", "Killemorgiri", "Kisrule", "Kocharewadi", "Kodal", "Kokisare", "Kolagewadi", "Kolane", "Kolekarwadi", "Kondhavale", "Konjavade", "Korivale", "Kotawadewadi", "Kumbhargaon", "Kusavade", "Kushi", "Kuthare", "Lendhori", "Letamewadi", "Loharwadi", "Lotalewadi", "Lugadewadi", "Mahind", "Majgaon", "Mala", "Maldan", "Malharpeth", "Maloshi", "Manainagar", "Mandrulkole", "Mandrulkole Kh.", "Mandure", "Maneri", "Manewadi", "Manyachiwadi", "Manyachiwadi", "Marali", "Marathwadi", "Marathwadi", "Marekarwadi", "Marloshi", "Marul Haveli", "Marul Tarf Patan", "Maskarwadi", "Maskarwadi", "Maskarwadi No. 1", "Mastewadi", "Mathanewadi", "Matrewadi", "Maulinagar", "Maundrul Haveli", "Mendh", "Mendheghar", "Mendhoshi", "Mharwand", "Mhavashi", "Mirgaon", "Modakwadi", "Morewadi (Kuthare)", "Morgiri", "Mulgaon", "Murud", "Muttalwadi", "Nade", "Nadoli", "Nahimbe", "Nanegaon Bk.", "Nanegaon Kh.", "Nanel", "Naralwadi", "Natoshi", "Nav", "Navadi", "Navasarwadi", "Nawaja", "Nechal", "Nerale", "Nigade", "Nisare", "Nivade", "Nivi", "Nune", "Pabhalwadi", "Pachgani", "Pachupatewadi", "Padekarwadi", "Padharwadi Telewadi", "Padloshi", "Pagewadi", "Palashi", "Pandharwadi", "Paneri", "Paparde Bk", "Paparde Kh.", "Patharpunj", "Pathavade", "Pawarwadi", "Pawarwadi", "Petekarwadi", "Pethshivapur", "Pimpaloshi", "Punvali", "Rahude", "Ramishtewadi", "Rasati", "Risawad", "Ruvale", "Sabalewadi", "Sadawaghapur", "Saikade", "Sakhari", "Salave", "Saltewadi", "Sanbur", "Sangwad", "Satar", "Sawantwadi", "Sawarghar", "Shedgewadi", "Shendewadi (Kumbhargaon)", "Shibewadi", "Shidrukwadi", "Shindewadi", "Shinganwadi", "Shiral", "Shirshinge", "Shitapwadi", "Shivandeshwar", "Siddheshwar Nagar", "Sonaichiwadi", "Sonavade", "Subhashnagar", "Sulewadi", "Supugadewadi", "Surul", "Sutarwadi", "Taliye", "Tamine", "Tamkade", "Tamkane", "Tarale", "Taygadewadi", "Telewadi", "Thankal", "Thomase", "Tolewadi", "Tondoshi", "Torane", "Tripudi", "Tupewadi", "Udhavane", "Umarkanchan", "Urul", "Vadi Kotawade", "Vaichalwadi", "Vajegaon", "Vajegaon", "Vajroshi", "Van", "Vanzole", "Varekarwadi", "Varpewadi", "Vatole", "Vekhandwadi", "Vetalwadi", "Vihe", "Virewadi", "Vittalwadi", "Waghane", "Wagjaiwadi", "Wazoli", "Yelavewadi", "Yerad", "Yeradwadi", "Yerphale", "Zadoli", "Zakade"

            )),
            Taluka("Phaltan", listOf(
                "Adarki Bk.", "Adarki Kh.", "Alagudewadi", "Aljapur", "Andrud", "Aradgaon", "Asu", "Bagewadi", "Barad", "Bhadali Bk.", "Bhadali Kh.", "Bhawaninagar (N. V)", "Bhilkatti", "Bibi", "Bodkewadi", "Chambharwadi", "Chaudharwadi", "Chavhanwadi", "Dalvadi", "Dattanagar (N. V)", "Dhaval", "Dhavalewadi", "Dhavalewadi", "Dhuldev", "Dhumalwadi", "Dombalwadi", "Dudhebavi", "Fadatarwadi", "Farandwadi", "Ghadge Mala", "Ghadgewadi", "Girvi", "Gokhali", "Gunware", "Hanmantwadi", "Hingangaon", "Hol", "Jadhavwadi", "Jadhavwadi", "Jaoli", "Jinti", "Kalaj", "Kambleshwar", "Kapadgaon", "Kapashi", "Kashiwadi", "Khadaki", "Khamgaon", "Kharadewadi", "Khatkewasti", "Khunte", "Koparde", "Koregaon", "Korhale", "Kurvali Bk.", "Kurvali Kh.", "Kusur", "Malewadi", "Malvadi", "Mandavkhadak", "Manewadi", "Mathachiwadi", "Mirdhe", "Mirewadi", "Mirewadi", "Mirgaon", "Mulikwadi", "Munjwadi", "Murum", "Naik Bombawadi", "Nandal", "Nimbhore", "Nimblak", "Nirugudi", "Padegaon", "Pawarwadi", "Pimpalwadi", "Pimparad", "Pirachiwadi", "Rajale", "Rajuri", "Ravadi Bk.", "Ravadi Kh.", "Salpe", "Sangavi", "Sarade", "Saskal", "Sastewadi", "Saswad", "Sathe", "Sherechiwadi", "Sherechiwadi", "Shereshindewadi", "Shindemal", "Shindenagar", "Shindewadi", "Somanthali", "Songaon", "Sonwadi Bk.", "Sonwadi Kh.", "Survadi", "Tadavale", "Takalwade", "Tambave", "Taradgaon", "Tardaf", "Tathavada", "Tavadi", "Thakubaichiwadi", "Thakurki", "Tirakwadi", "Upalave", "Vadale", "Vadgaon", "Vadjal", "Vajegaon (N. V)", "Veloshi", "Vidani", "Vinchurni", "Vitthalwadi", "Waghoshi", "Wakhari", "Wathar (Nimbalkar)", "Zadakbaichiwadi", "Zirapwadi"

            )),
            Taluka("Satara", listOf(
                "Agudewadi", "Akale", "Alawadi", "Ambale", "Ambavade Bk", "Ambavade Kh.", "Ambewadi", "Anavale", "Angapur T Targaon", "Angapur Vandan", "Apashinge", "Arale", "Are T. Parali", "Argadwadi", "Arphal", "Asangaon", "Asgaon", "Ashte", "Atali", "Atit", "Banghar", "Basappachiwadi", "Bendwadi", "Bhairavgad (N. V.)", "Bhambavali", "Bharatgaon", "Bharatgaonwadi", "Bhatmarali", "Bhondavade", "Boposhi", "Borgaon", "Borkhal", "Borne", "Bramhanwadi", "Chahur", "Chalkewadi", "Chikhali", "Chinchaner S. Nimb", "Chinchaner Vandan", "Chinchani", "Chorgewadi", "Dabewadi", "Dahivad", "Dare Bk.", "Dare Kh.", "Dare T. Parali", "Degaon", "Deshmukh Nagar", "Dhanavadewadi", "Dhangarwadi", "Dhangarwadi", "Dhavali", "Dhawadshi", "Dhondewadi", "Dhondewadi", "Didhavale", "Dolegaon", "Fadatarwadi", "Fatyapur", "Gajawadi", "Ganeshwadi", "Gavadi", "Ghatawan", "Gogavalewadi", "Gojegaon", "Gove", "Hamdabaj", "Jadhavwadi", "Jaitapur", "Jakatwadi", "Jambhe", "Jawalwadi", "Jihe", "Jotibachiwadi", "Kalambe", "Kaloshi", "Kamathi T. Parali", "Kamathi T. Satara", "Kameri", "Kanher", "Karandi", "Karandwadi", "Karanje T. Parali", "Karanjoshi", "Kari", "Kasani", "Kasarthal", "Kashil", "Katavadi Bk", "Katavadi Kh", "Kelavali", "Khadegaon", "Khandobachi Wadi", "Khavali", "Khindwadi", "Khodad", "Khojewadi", "Kidgaon", "Kondhave", "Kondvali", "Koparde", "Kshetra Mahuli", "Kudeghar", "Kumathe", "Kurulbaji", "Kurultijai", "Kurun", "Kus Bk.", "Kus Kh.", "Kusavade", "Kushi", "Landewadi", "Lavanghar", "Limb", "Limbachiwadi", "Lumanekhol", "Mahagaon", "Majgaon", "Malgaon", "Malyachi Wadi", "Mandave", "Mardhe", "Matyapur", "Mhaparwadi", "Mhasave", "Mhaskarwadi", "Morewadi", "Mugdul Bhatachiwadi", "Mulikwadi", "Nagewadi", "Nagthane", "Nandgaon", "Navali", "Nele", "Nhalewadi", "Nigadi T. Satara", "Nigadi Vandan", "Nigudamal", "Ninam", "Nisarale", "Nitral", "Nune", "Padali", "Palsavade", "Pangare", "Panmalewadi", "Parali", "Parambe", "Parmale", "Pateghar", "Patkhal", "Petri", "Pilani", "Pilaniwadi", "Pimpalwadi", "Pirwadi", "Pogarwadi", "Punavadi", "Raighar", "Rajapuri", "Rakusalewadi", "Ramkrushna Nagar", "Renavale", "Revali", "Revande", "Rohot", "Sabalewadi", "Saidapur", "Salwan", "Sambarwadi", "Sandavali", "Sangam Mahuli", "Sarkhal", "Saspade", "Savali", "Sayali", "Sayali", "Shahapur", "Shelkewadi", "Shendre", "Sherewadi", "Shindewadi", "Shivthar", "Sonapur", "Sonavadi", "Sonegaon T Satara", "Songaon S. Nimb", "Takawali", "Tasgaon", "Thoseghar", "Titavewadi", "Tukaichiwadi", "Upali", "Vaduth", "Valase", "Vangal", "Vanvaswadi", "Varne", "Varye", "Vechale", "Vele", "Venegaon", "Venekhol", "Vijay Nagar", "Wadgaon", "Wadhe", "Wasole", "Wavadare", "Yavateshwar", "Zarewadi"

            )),
            Taluka("Wai", listOf(
                "Abhepuri", "Akoshi", "Ambedara", "Amrutwadi", "Anavadi", "Anpatwadi", "Asagaon", "Asale", "Asare", "Badewadi (N. V.)", "Balakavadi", "Baleghar", "Bavdhan", "Belamachi", "Bhirdachiwadi (N. V.)", "Bhivadi", "Bhivadi (R. H. V.)", "Bhogaon", "Bhuinj", "Bopardi", "Bopegaon", "Borgaon Bk.", "Borgaon Kh.", "Boriv", "Chandak", "Chandvadi (R. H. V.)", "Chikhali", "Chindhawali", "Chorachiwadi", "Dahyat", "Darewadi", "Dasvadi", "Degaon", "Dhavadi", "Dhavali", "Dhom", "Duichivadi", "Ekasar", "Gadhavewadi", "Gherakelanja", "Golegaon", "Golewadi", "Gove", "Govedigar", "Gulumb", "Gundewadi", "Jamb", "Jambhali", "Jambhulane", "Jor", "Kadegaon", "Kalambhe", "Kalangwadi", "Kanur", "Kawathe", "Kenjal", "Khadaki", "Khalachi Belmachi", "Khanapur", "Khavali", "Kholvadi", "Kikali", "Kironde", "Kisanvirnagar", "Kochalewadi", "Kondhavale", "Kondhavali Bk", "Kondhavali Kh", "Kusgaon", "Logadwadi", "Lohare", "Malatpur", "Maldevwadi (N. V.)", "Malusurewadi", "Mandhardeo", "Maparwadi / Vapanwadi", "Menavali", "Mohodekarwadi", "Mugaon", "Mungasewadi", "Nagewadi", "Nandgane", "Nhalewadi", "Nikamwadi", "Oholi", "Ozarde", "Pachputewadi (N. V.)", "Panas", "Panchwad", "Pande", "Pandewadi", "Pandharechiwadi", "Paratavadi", "Parkhandi", "Pasarni", "Pirachiwadi", "Purna Vyahali (Rhv)", "Rautwadi", "Renawale", "Satalewadi", "Shahabag (N. V.)", "Shelarwadi", "Shendurjane", "Shirgaon", "Sidhanathwadi (Rural)", "Sultanpur", "Surur", "Udatare", "Ulumb", "Vadoli", "Vahagaon", "Vaigaon", "Varkhadwadi", "Vasole", "Velang", "Vele", "Virmade", "Vithalwadi", "Vyahali", "Vyajawadi", "Wadachiwadi (N. V.)", "Wadkarwadi", "Wai (Rural)", "Washivali", "Yashwantnagar (N. V.)", "Yeruli"

            ))
        )

        _talukas.value = sataraTalukas
    }

    fun setSelectedTaluka(talukaName: String?) {
        _selectedTaluka.value = talukaName
        if (talukaName != null) {
            // Update villages list based on selected taluka
            val selectedTalukaData = _talukas.value.find { it.name == talukaName }
            _villages.value = selectedTalukaData?.villages ?: emptyList()
            _filteredVillages.value = _villages.value
        } else {
            _villages.value = emptyList()
            _filteredVillages.value = emptyList()
        }
    }

    fun searchVillage(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _filteredVillages.value = _villages.value
            } else {
                _filteredVillages.value = _villages.value.filter { village ->
                    village.startsWith(query, ignoreCase = true)
                }
            }
        }
    }

    private fun initializeFoodsByCategory() {
        _foodsByCategory.value = mapOf(
            Category.FRUITS to listOf("Grapes", "Strawberries"),
            Category.VEGETABLES to listOf("Tomatoes", "Potatoes"),
            Category.GRAINS to listOf("Wheat", "Rice", "Corn", "Jowar"),
            Category.OILSEEDS to listOf("Soybean")
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: Category?) {
        _selectedCategory.value = category
    }

    fun setSelectedProduct(product: ListedCrop?) {
        _selectedProduct.value = product
    }

    fun setCustomRate(rate: String) {
        _customRate.value = rate
    }

    fun getEffectiveRate(): Double {
        return _customRate.value.toDoubleOrNull() ?: _selectedProduct.value?.rate?.toDouble() ?: 0.0
    }

    private fun setupRealTimeListeners() {
        println("Debug: Setting up real-time listeners")
        
        // Listen for all crops
        cropsListener = cropsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
            if (error != null) {
                    println("Debug: Error listening to crops: ${error.message}")
                _error.value = "Error listening to crops: ${error.message}"
                return@addSnapshotListener
            }

            snapshot?.let { querySnapshot ->
                    println("Debug: Received ${querySnapshot.documents.size} crops")
                val cropsList = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data
                            println("Debug: Processing crop document: ${doc.id}")
                            println("Debug: Crop data: $data")
                            
                        if (data != null) {
                            ListedCrop(
                                id = doc.id,
                                name = data["name"]?.toString() ?: "",
                                quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                                rate = (data["rate"] as? Number)?.toDouble() ?: 0.0,
                                location = data["location"]?.toString() ?: "",
                                    category = try {
                                        Category.valueOf((data["category"] as? String)?.uppercase() ?: Category.GRAINS.name)
                                    } catch (e: Exception) {
                                        println("Debug: Error parsing category: ${e.message}")
                                        Category.GRAINS
                                    },
                                sellerName = data["sellerName"]?.toString() ?: "Unknown Seller",
                                sellerContact = data["sellerContact"]?.toString() ?: "No contact provided",
                                    timestamp = data["timestamp"] as? Timestamp ?: data["createdAt"] as? Timestamp,
                                    status = data["status"]?.toString() ?: "available",
                                    isOwnListing = data["sellerEmail"] == AuthHelper.getCurrentUserEmail(),
                                    description = data["description"]?.toString() ?: "",
                                    price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                                    imageUrl = data["imageUrl"]?.toString() ?: "",
                                    farmerId = data["farmerId"]?.toString() ?: "",
                                    farmerName = data["farmerName"]?.toString() ?: "",
                                    createdAt = data["createdAt"] as? Timestamp,
                                    rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,
                                    totalRatings = (data["totalRatings"] as? Number)?.toInt() ?: 0
                            )
                        } else null
                    } catch (e: Exception) {
                            println("Debug: Error parsing crop data: ${e.message}")
                        _error.value = "Error parsing crop data: ${e.message}"
                        null
                    }
                }
                    println("Debug: Processed ${cropsList.size} valid crops")
                _listedCrops.value = cropsList
            }
        }

        // Listen for user's own crops
        val currentUserEmail = AuthHelper.getCurrentUserEmail()
        if (currentUserEmail != null) {
            myCropsListener = cropsCollection
                .whereEqualTo("sellerEmail", currentUserEmail)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _error.value = "Error listening to your crops: ${error.message}"
                        return@addSnapshotListener
                    }

                    snapshot?.let { querySnapshot ->
                        val crops = querySnapshot.documents.mapNotNull { doc ->
                            val data = doc.data
                            if (data != null) {
                                ListedCrop(
                                    id = doc.id,
                                    name = data["name"] as? String ?: "",
                                    quantity = (data["quantity"] as? Long)?.toInt() ?: 0,
                                    rate = (data["rate"] as? Long)?.toDouble() ?: 0.0,
                                    location = data["location"] as? String ?: "",
                                    category = Category.valueOf(data["category"] as? String ?: Category.GRAINS.name),
                                    sellerName = data["sellerName"] as? String ?: "Unknown Seller",
                                    sellerContact = data["sellerContact"] as? String ?: "No contact provided",
                                    timestamp = null,
                                    buyerDetails = (data["buyerDetails"] as? List<Map<String, Any>>)?.map { buyer ->
                                        BuyerDetail(
                                            name = buyer["name"]?.toString() ?: "",
                                            contactInfo = buyer["contactInfo"]?.toString() ?: "",
                                            address = buyer["address"]?.toString() ?: "",
                                            requestedQuantity = (buyer["requestedQuantity"] as? Long)?.toInt() ?: 0,
                                            status = buyer["status"]?.toString() ?: "PENDING"
                                        )
                                    } ?: emptyList(),
                                    isOwnListing = true
                                )
                            } else null
                        }
                        _myListedCrops.value = crops
                    }
                }
        }

        // Listen for purchase requests
        setupPurchaseRequestsListener()
    }

    private fun setupPurchaseRequestsListener() {
        viewModelScope.launch {
            try {
                println("Debug: Setting up purchase requests listener")
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    println("Debug: No authenticated user found")
                    _error.value = "User not authenticated"
                    return@launch
                }

                // Listen for purchase requests where user is the buyer
            purchaseRequestsListener = db.collection("purchase_requests")
                    .whereEqualTo("buyerEmail", currentUser.email)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                            println("Debug: Error listening to purchase requests: ${error.message}")
                            _error.value = "Error fetching purchase requests: ${error.message}"
                        return@addSnapshotListener
                    }

                        println("Debug: Received purchase requests snapshot. Documents count: ${snapshot?.documents?.size ?: 0}")
                        
                        val requests = snapshot?.documents?.mapNotNull { doc ->
                            try {
                                println("Debug: Processing purchase request document: ${doc.id}")
                                println("Debug: Document data: ${doc.data}")
                                PurchaseRequest.fromMap(doc.id, doc.data ?: emptyMap())
                            } catch (e: Exception) {
                                println("Debug: Error parsing purchase request: ${e.message}")
                                _error.value = "Error parsing purchase request: ${e.message}"
                                null
                            }
                        } ?: emptyList()
                        
                        println("Debug: Processed ${requests.size} valid purchase requests")
                        _purchaseRequests.value = requests
                    }

                setupSellerPurchaseRequestsListener()
            } catch (e: Exception) {
                println("Debug: Error in setupPurchaseRequestsListener: ${e.message}")
                e.printStackTrace()
                _error.value = "Error setting up purchase requests listener: ${e.message}"
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up listeners
        cropsListener?.remove()
        myCropsListener?.remove()
        purchaseRequestsListener?.remove()
        acceptedRequestsListener?.remove()
    }

    fun selectCrop(cropId: String) {
        viewModelScope.launch {
            try {
                // Find the crop in the current listings
                val crop = _listedCrops.value.find { it.id == cropId }
                if (crop != null) {
                    _selectedCrop.value = crop
                    return@launch
                }

                // If not found in current listings, try to fetch from Firestore
                val cropDoc = cropsCollection.document(cropId).get().await()
                
                if (cropDoc.exists()) {
                    val data = cropDoc.data
                    if (data != null) {
                        val currentUser = AuthHelper.getCurrentUserEmail()
                        
                        val selectedCrop = ListedCrop(
                            id = cropDoc.id,
                            name = data["name"]?.toString() ?: "",
                            quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                            rate = (data["rate"] as? Number)?.toDouble() ?: 0.0,
                            location = data["location"]?.toString() ?: "",
                            category = Category.valueOf((data["category"] as? String) ?: Category.GRAINS.name),
                            sellerName = data["sellerName"]?.toString() ?: "Unknown Seller",
                            sellerContact = data["sellerContact"]?.toString() ?: "No contact provided",
                            timestamp = (data["timestamp"] as? Timestamp),
                            buyerDetails = (data["buyerDetails"] as? List<Map<String, Any>>)?.map { buyer ->
                                BuyerDetail(
                                    name = buyer["name"]?.toString() ?: "",
                                    contactInfo = buyer["contactInfo"]?.toString() ?: "",
                                    address = buyer["address"]?.toString() ?: "",
                                    requestedQuantity = (buyer["requestedQuantity"] as? Number)?.toInt() ?: 0,
                                    status = buyer["status"]?.toString() ?: "PENDING"
                                )
                            } ?: emptyList()
                        )
                        
                        _selectedCrop.value = selectedCrop
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to select crop: ${e.message}"
            }
        }
    }

    fun clearSelectedCrop() {
        _selectedCrop.value = null
    }

    fun sendPurchaseRequest(cropId: String, buyerDetail: BuyerDetail) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                println("Debug: Starting purchase request process")
                
                // Validate buyer details
                if (buyerDetail.name.isBlank() || 
                    buyerDetail.contactInfo.isBlank() || 
                    buyerDetail.address.isBlank() || 
                    buyerDetail.requestedQuantity <= 0) {
                    throw Exception("Please fill in all required fields: Name, Contact, Address, and Quantity")
                }

                val currentUser = AuthHelper.getCurrentUserEmail() ?: throw Exception("User not logged in")
                val currentUserId = AuthHelper.getCurrentUserId() ?: throw Exception("User ID not found")

                // Get crop details first
                val cropDoc = db.collection("crops").document(cropId).get().await()
                val cropData = cropDoc.data ?: throw Exception("Crop not found")
                
                val requestData = hashMapOf(
                    "cropId" to cropId,
                    "cropName" to cropData["name"]?.toString(),
                    "buyerId" to currentUserId,
                    "buyerName" to buyerDetail.name,
                    "buyerContact" to buyerDetail.contactInfo,
                    "buyerEmail" to currentUser,
                    "sellerId" to cropData["sellerId"]?.toString(),
                    "sellerName" to cropData["sellerName"]?.toString(),
                    "sellerEmail" to cropData["sellerEmail"]?.toString(),
                    "requestedQuantity" to buyerDetail.requestedQuantity,
                    "acceptedQuantity" to 0,
                    "totalAmount" to (buyerDetail.requestedQuantity * ((cropData["rate"] as? Number)?.toDouble() ?: 0.0)),
                    "status" to "pending",
                    "createdAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now(),
                    "deliveryAddress" to buyerDetail.address
                )

                println("Debug: Sending request data to cloud function: $requestData")

                try {
                    // First try cloud function
                    println("Debug: Attempting to create purchase request via cloud function")
                    val result = functions
                        .getHttpsCallable("createPurchaseRequest")
                        .call(requestData)
                        .await()
                    println("Debug: Cloud function response: $result")
                } catch (e: Exception) {
                    println("Debug: Cloud function failed, falling back to direct creation: ${e.message}")
                    
                    // Fallback: Create directly in Firestore
                    val docRef = db.collection("purchase_requests").add(requestData).await()
                    println("Debug: Created purchase request directly with ID: ${docRef.id}")

                    // Also update the crop's buyerDetails
                    val currentRequests = (cropData["buyerDetails"] as? List<Map<String, Any>> ?: emptyList()).toMutableList()
                    currentRequests.add(
                        hashMapOf(
                        "name" to buyerDetail.name,
                        "contactInfo" to buyerDetail.contactInfo,
                        "address" to buyerDetail.address,
                            "requestedQuantity" to buyerDetail.requestedQuantity,
                            "status" to "PENDING"
                        )
                    )
                    
                    db.collection("crops").document(cropId)
                        .update("buyerDetails", currentRequests)
                        .await()
                }

                // Refresh data
                refresh()
                setupPurchaseRequestsListener()

            } catch (e: Exception) {
                println("Debug: Error in sendPurchaseRequest: ${e.message}")
                e.printStackTrace()
                _error.value = "Failed to send purchase request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Refresh all data
                setupRealTimeListeners()
            } catch (e: Exception) {
                _error.value = "Failed to refresh data: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun acceptBuyerRequest(requestId: String, acceptedQuantity: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                println("Debug: Starting request acceptance process")
                
                // Get the purchase request
                val requestDoc = db.collection("purchase_requests").document(requestId).get().await()
                val requestData = requestDoc.data ?: throw Exception("Purchase request not found")
                
                val cropId = requestData["cropId"]?.toString() ?: throw Exception("Crop ID not found")
                val requestedQuantity = (requestData["requestedQuantity"] as? Number)?.toInt() ?: 0
                
                // Validate accepted quantity
                if (acceptedQuantity <= 0 || acceptedQuantity > requestedQuantity) {
                    throw Exception("Invalid accepted quantity. Must be between 1 and $requestedQuantity")
                }
                
                // Get the crop document
                val cropDoc = db.collection("crops").document(cropId).get().await()
                val cropData = cropDoc.data ?: throw Exception("Crop not found")
                val currentQuantity = (cropData["quantity"] as? Number)?.toInt() ?: 0
                
                if (acceptedQuantity > currentQuantity) {
                    throw Exception("Cannot accept more than available quantity ($currentQuantity kg)")
                }
                
                // Update purchase request
                db.collection("purchase_requests").document(requestId)
                    .update(
                        mapOf(
                            "status" to "accepted",
                            "acceptedQuantity" to acceptedQuantity,
                            "updatedAt" to Timestamp.now()
                        )
                    ).await()
                
                // Update crop quantity
                val newQuantity = currentQuantity - acceptedQuantity
                db.collection("crops").document(cropId)
                    .update(
                        mapOf(
                            "quantity" to newQuantity,
                            "updatedAt" to Timestamp.now()
                        )
                    ).await()
                
                // Update buyer details in crop document
                val buyerDetails = (cropData["buyerDetails"] as? List<Map<String, Any>> ?: emptyList()).toMutableList()
                val buyerIndex = buyerDetails.indexOfFirst { 
                    it["name"] == requestData["buyerName"] && it["status"] == "PENDING"
                }
                
                if (buyerIndex != -1) {
                    buyerDetails[buyerIndex] = mapOf(
                        "name" to (requestData["buyerName"] ?: ""),
                        "contactInfo" to (requestData["buyerContact"] ?: ""),
                        "address" to (requestData["deliveryAddress"] ?: ""),
                        "requestedQuantity" to acceptedQuantity,
                        "status" to "ACCEPTED"
                    )
                    
                    db.collection("crops").document(cropId)
                        .update("buyerDetails", buyerDetails)
                        .await()
                }

                // If quantity is now 0, mark crop as sold
                if (newQuantity == 0) {
                    db.collection("crops").document(cropId)
                        .update("status", "sold")
                        .await()
                }

                println("Debug: Successfully accepted purchase request")
                refresh() // Refresh all data

            } catch (e: Exception) {
                println("Debug: Error accepting purchase request: ${e.message}")
                e.printStackTrace()
                _error.value = "Failed to accept purchase request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteListedCrop(cropId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = hashMapOf("cropId" to cropId)
                val result = functions.getHttpsCallable("deleteCrop").call(data).await()
                refresh() // Refresh the data after deleting
            } catch (e: Exception) {
                _error.value = "Failed to delete crop: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getTodaysRate(cropName: String): Double {
        return when (cropName.lowercase()) {
            // Grains
            "wheat" -> 2500.0
            "rice" -> 3000.0
            "corn" -> 2000.0
            "jowar" -> 2800.0
            // Vegetables
            "tomatoes" -> 50.0
            "potatoes" -> 30.0
            // Fruits
            "grapes" -> 100.0
            "strawberries" -> 200.0
            // Oilseeds
            "soybean" -> 4500.0
            else -> 0.0
        }
    }

    fun addCrop(
        name: String,
        quantity: Int,
        rate: Double,
        location: String,
        category: Category,
        sellerName: String,
        sellerContact: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                println("Debug: Adding crop with name: $name, category: $category") // Debug log
                
                val currentUser = AuthHelper.getCurrentUserEmail()
                val currentUserId = AuthHelper.getCurrentUserId()
                
                if (currentUser == null || currentUserId == null) {
                    _error.value = "User must be logged in to add crops"
                    return@launch
                }

                val initialStatus = if (quantity <= 0) "unavailable" else "available"

                val cropData = hashMapOf(
                    "name" to name,
                    "quantity" to quantity,
                    "rate" to rate,
                    "location" to location,
                    "category" to category.name,
                    "sellerName" to sellerName,
                    "sellerContact" to sellerContact,
                    "sellerEmail" to currentUser,
                    "sellerId" to currentUserId,
                    "status" to initialStatus,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
                
                println("Debug: Adding crop directly to Firestore: $cropData") // Debug log
                
                // Try adding directly to Firestore first
                try {
                    val docRef = db.collection("crops").add(cropData).await()
                    println("Debug: Successfully added crop to Firestore with ID: ${docRef.id}")
                } catch (e: Exception) {
                    println("Debug: Error adding crop to Firestore directly: ${e.message}")
                    
                    // If direct add fails, try through Cloud Function
                    println("Debug: Trying through Cloud Function")
                    val result = functions
                        .getHttpsCallable("addCrop")
                        .call(cropData)
                        .await()
                    println("Debug: Cloud function result: $result")
                }
                
                // Refresh the data after adding
                setupRealTimeListeners()
            } catch (e: Exception) {
                println("Debug: Error adding crop: ${e.message}") // Debug log
                _error.value = "Failed to add crop: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelPurchaseRequest(requestId: String, quantity: Int, reason: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get the purchase request first
                val requestDoc = db.collection("purchase_requests").document(requestId).get().await()
                val requestData = requestDoc.data ?: throw Exception("Purchase request not found")
                
                // Update the purchase request status to cancelled
                db.collection("purchase_requests").document(requestId)
                    .update(
                        mapOf(
                            "status" to "cancelled",
                            "cancelReason" to reason,
                            "updatedAt" to Timestamp.now()
                        )
                    ).await()

                // Remove the request from the local list
                _purchaseRequests.value = _purchaseRequests.value.filter { it.id != requestId }

                // Refresh the data to ensure UI is updated
                refresh()
                
            } catch (e: Exception) {
                println("Debug: Error cancelling purchase request: ${e.message}")
                e.printStackTrace()
                _error.value = "Failed to cancel purchase request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCrops() {
        viewModelScope.launch {
            try {
                val cropsSnapshot = db.collection("crops").get().await()
                val cropsList = cropsSnapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    
                    // Helper function to safely convert to Double
                    fun getDoubleValue(value: Any?): Double {
                        return when (value) {
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                    }
                    
                    val quantity = (data["quantity"] as? Number)?.toInt() ?: 0
                    val status = data["status"] as? String ?: "available"
                    
                    // Include the crop if it has quantity > 0 OR if it's marked as sold_out
                    if (quantity <= 0 && status != "sold_out") return@mapNotNull null
                    
                    ListedCrop(
                        id = doc.id,
                        name = data["name"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        price = getDoubleValue(data["price"]),
                        quantity = quantity,
                        category = Category.valueOf(data["category"] as? String ?: Category.OTHER.name),
                        imageUrl = data["imageUrl"] as? String ?: "",
                        farmerId = data["farmerId"]  as? String ?: "",
                        farmerName = data["farmerName"] as? String ?: "",
                        createdAt = data["createdAt"] as? Timestamp,
                        status = status,
                        rating = getDoubleValue(data["rating"]),
                        totalRatings = (data["totalRatings"] as? Number)?.toInt() ?: 0,
                        rate = getDoubleValue(data["rate"]),
                        location = data["location"] as? String ?: "",
                        sellerName = data["sellerName"] as? String ?: "Unknown Seller",
                        sellerContact = data["sellerContact"] as? String ?: "No contact provided",
                        isOwnListing = data["isOwnListing"] as? Boolean ?: false,
                        buyerDetails = (data["buyerDetails"] as? List<Map<String, Any>>)?.map { buyer ->
                            BuyerDetail(
                                name = buyer["name"]?.toString() ?: "",
                                contactInfo = buyer["contactInfo"]?.toString() ?: "",
                                address = buyer["address"]?.toString() ?: "",
                                requestedQuantity = (buyer["requestedQuantity"] as? Number)?.toInt() ?: 0,
                                status = buyer["status"]?.toString() ?: "PENDING"
                            )
                        } ?: emptyList(),
                        timestamp = data["timestamp"] as? Timestamp
                    )
                }
                _crops.value = cropsList
            } catch (e: Exception) {
                _error.value = "Error loading crops: ${e.message}"
            }
        }
    }

    fun filterCropsByCategory(category: Category?) {
        _selectedCategory.value = category
        viewModelScope.launch {
            try {
                val query = if (category != null) {
                    db.collection("crops").whereEqualTo("category", category.name)
                } else {
                    db.collection("crops")
                }
                
                val cropsSnapshot = query.get().await()
                val cropsList = cropsSnapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val quantity = (data["quantity"] as? Number)?.toInt() ?: 0
                    val status = data["status"] as? String ?: "available"
                    
                    // Include the crop if it has quantity > 0 OR if it's marked as sold_out
                    if (quantity <= 0 && status != "sold_out") return@mapNotNull null
                    
                    ListedCrop(
                        id = doc.id,
                        name = data["name"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                        quantity = quantity,
                        category = Category.valueOf(data["category"] as? String ?: Category.OTHER.name),
                        imageUrl = data["imageUrl"] as? String ?: "",
                        farmerId = data["farmerId"] as? String ?: "",
                        farmerName = data["farmerName"] as? String ?: "",
                        createdAt = data["createdAt"] as? Timestamp,
                        status = status,
                        rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,
                        totalRatings = (data["totalRatings"] as? Number)?.toInt() ?: 0,
                        rate = (data["rate"] as? Number)?.toDouble() ?: 0.0,
                        location = data["location"] as? String ?: "",
                        sellerName = data["sellerName"] as? String ?: "Unknown Seller",
                        sellerContact = data["sellerContact"] as? String ?: "No contact provided",
                        isOwnListing = data["isOwnListing"] as? Boolean ?: false,
                        buyerDetails = (data["buyerDetails"] as? List<Map<String, Any>>)?.map { buyer ->
                            BuyerDetail(
                                name = buyer["name"]?.toString() ?: "",
                                contactInfo = buyer["contactInfo"]?.toString() ?: "",
                                address = buyer["address"]?.toString() ?: "",
                                requestedQuantity = (buyer["requestedQuantity"] as? Number)?.toInt() ?: 0,
                                status = buyer["status"]?.toString() ?: "PENDING"
                            )
                        } ?: emptyList(),
                        timestamp = data["timestamp"] as? Timestamp
                    )
                }
                _crops.value = cropsList
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun createCrop(crop: ListedCrop) {
        viewModelScope.launch {
            try {
                val cropData = hashMapOf(
                    "name" to crop.name,
                    "description" to crop.description,
                    "price" to crop.price,
                    "quantity" to crop.quantity,
                    "category" to crop.category.name,
                    "imageUrl" to crop.imageUrl,
                    "farmerId" to crop.farmerId,
                    "farmerName" to crop.farmerName,
                    "rate" to crop.rate,
                    "location" to crop.location,
                    "sellerName" to crop.sellerName,
                    "sellerContact" to crop.sellerContact,
                    "timestamp" to Timestamp.now(),
                    "status" to if (crop.quantity <= 0) "unavailable" else "available",
                    "isOwnListing" to false
                )
                
                db.collection("crops").add(cropData).await()
                loadCrops()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateCropStatus(cropId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                db.collection("crops").document(cropId)
                    .update("status", newStatus)
                    .await()
                loadCrops()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // Temporary function to migrate data
    fun migrateData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                functions.getHttpsCallable("migrateData")
                    .call()
                    .await()
                println("Migration completed successfully")
                refresh() // Refresh the data after migration
            } catch (e: Exception) {
                println("Migration failed: ${e.message}")
                _error.value = "Migration failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateCropStatusBasedOnQuantity(cropId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                val newStatus = if (quantity <= 0) "unavailable" else "available"
                db.collection("crops").document(cropId)
                    .update("status", newStatus)
                    .addOnSuccessListener {
                        println("Debug: Updated crop status to $newStatus for crop $cropId")
                    }
                    .addOnFailureListener { e ->
                        println("Debug: Failed to update crop status: ${e.message}")
                        _error.value = "Failed to update crop status: ${e.message}"
                    }
            } catch (e: Exception) {
                println("Debug: Error updating crop status: ${e.message}")
                _error.value = "Error updating crop status: ${e.message}"
            }
        }
    }

    fun updateCropQuantity(cropId: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                db.collection("crops").document(cropId)
                    .update("quantity", newQuantity)
                    .addOnSuccessListener {
                        println("Debug: Updated crop quantity to $newQuantity for crop $cropId")
                        // Update status based on new quantity
                        updateCropStatusBasedOnQuantity(cropId, newQuantity)
                    }
                    .addOnFailureListener { e ->
                        println("Debug: Failed to update crop quantity: ${e.message}")
                        _error.value = "Failed to update crop quantity: ${e.message}"
                    }
            } catch (e: Exception) {
                println("Debug: Error updating crop quantity: ${e.message}")
                _error.value = "Error updating crop quantity: ${e.message}"
            }
        }
    }

    // Add this function to fetch the price
    fun fetchTodaysRate(commodity: String) {
        viewModelScope.launch {
            _isLoadingPrice.value = true
            try {
                // You might want to get the state from user's profile or settings
                val state = "Maharashtra" // Default state, you can make this configurable
                val price = AgriPriceService.getCropPrice(commodity, state)
                _todaysRate.value = price
            } catch (e: Exception) {
                _error.value = "Failed to fetch today's rate: ${e.message}"
            } finally {
                _isLoadingPrice.value = false
            }
        }
    }

    // Add this function to clear the rate when changing crops
    fun clearTodaysRate() {
        _todaysRate.value = null
    }

    fun updateCropStatus(cropId: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                if (newQuantity <= 0) {
                    // Instead of deleting, mark the crop as sold_out
                    db.collection("crops").document(cropId)
                        .update(
                            mapOf(
                                "quantity" to 0,
                                "status" to "sold_out"
                            )
                        )
                        .await()
                    // Update the local list by updating the crop status
                    _crops.value = _crops.value.map { crop ->
                        if (crop.id == cropId) {
                            crop.copy(quantity = 0, status = "sold_out")
                        } else {
                            crop
                        }
                    }
                } else {
                    // Update the quantity in the database
                    db.collection("crops").document(cropId)
                        .update(
                            mapOf(
                                "quantity" to newQuantity,
                                "status" to "available"
                            )
                        )
                        .await()
                    // Update the local list
                    _crops.value = _crops.value.map { crop ->
                        if (crop.id == cropId) {
                            crop.copy(quantity = newQuantity, status = "available")
                        } else {
                            crop
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error updating crop status: ${e.message}"
            }
        }
    }
}
