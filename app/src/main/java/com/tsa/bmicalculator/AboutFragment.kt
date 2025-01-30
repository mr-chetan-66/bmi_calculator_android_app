package com.tsa.bmicalculator

import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

// Data class to hold section information
data class AboutSection(
    val title: String,
    @DrawableRes val iconRes: Int,
    val content: Spanned,
    var isExpanded: Boolean = false
)

class AboutFragment : Fragment() {
    private lateinit var sectionsAdapter: AboutSectionsAdapter
    private val sections = mutableListOf<AboutSection>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)
        populateSections()
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.sections_recycler_view)
        sectionsAdapter = AboutSectionsAdapter(sections)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sectionsAdapter
            setHasFixedSize(true)
        }
    }

    private fun populateSections() {
        sections.apply {
            // BMI Formula Section
            add(AboutSection(
                "BMI Formula",
                R.drawable.ic_calculator,
                Html.fromHtml(getString(R.string.bmi_formula_content), Html.FROM_HTML_MODE_COMPACT)
            ))

            // FAQs Section
            add(AboutSection(
                "FAQs",
                R.drawable.ic_help,
                Html.fromHtml(getString(R.string.faq_content), Html.FROM_HTML_MODE_COMPACT)
            ))

            // How to Improve BMI Section
            add(AboutSection(
                "How to Improve BMI",
                R.drawable.ic_improvement,
                Html.fromHtml(getString(R.string.improve_bmi_content), Html.FROM_HTML_MODE_COMPACT)
            ))

            // Health Risks Section
            add(AboutSection(
                "Health Risks",
                R.drawable.ic_warning,
                Html.fromHtml(getString(R.string.health_risks_content), Html.FROM_HTML_MODE_COMPACT)
            ))

            // Side Effects Section
            add(AboutSection(
                "Side Effects",
                R.drawable.ic_side_effects,
                Html.fromHtml(getString(R.string.side_effects_content), Html.FROM_HTML_MODE_COMPACT)
            ))

            // History Section
            add(AboutSection(
                "History of BMI",
                R.drawable.ic_history,
                Html.fromHtml(getString(R.string.history_content), Html.FROM_HTML_MODE_COMPACT)
            ))

            // Developer Info Section
            add(AboutSection(
                "Developer Info",
                R.drawable.ic_developer,
                Html.fromHtml(getString(R.string.developer_info_content), Html.FROM_HTML_MODE_COMPACT)
            ))

            // Terms and Conditions Section
            add(AboutSection(
                "Terms & Conditions",
                R.drawable.ic_terms,
                Html.fromHtml(getString(R.string.terms_conditions_content), Html.FROM_HTML_MODE_COMPACT)
            ))

            // Legal Information Section
            add(AboutSection(
                "Legal Information",
                R.drawable.ic_legal,
                Html.fromHtml(getString(R.string.legal_info_content), Html.FROM_HTML_MODE_COMPACT)
            ))

            // Privacy Policy Section
            add(AboutSection(
                "Privacy Policy",
                R.drawable.ic_privacy,
                Html.fromHtml(getString(R.string.privacy_policy_content), Html.FROM_HTML_MODE_COMPACT)
            ))

            // Copyright Section
            add(AboutSection(
                "Copyright",
                R.drawable.ic_copyright,
                Html.fromHtml(getString(R.string.copyright_content), Html.FROM_HTML_MODE_COMPACT)
            ))
        }
        sectionsAdapter.notifyDataSetChanged()
    }
}

// Adapter for the RecyclerView
class AboutSectionsAdapter(
    private val sections: List<AboutSection>
) : RecyclerView.Adapter<AboutSectionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_about_section, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val section = sections[position]
        holder.bind(section)
    }

    override fun getItemCount() = sections.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerLayout: View = itemView.findViewById(R.id.header_layout)
        private val sectionIcon: ImageView = itemView.findViewById(R.id.section_icon)
        private val sectionTitle: TextView = itemView.findViewById(R.id.section_title)
        private val expandIcon: ImageView = itemView.findViewById(R.id.expand_icon)
        private val contentCard: MaterialCardView = itemView.findViewById(R.id.content_card)
        private val sectionContent: TextView = itemView.findViewById(R.id.section_content)

        fun bind(section: AboutSection) {
            sectionIcon.setImageResource(section.iconRes)
            sectionTitle.text = section.title
            sectionContent.text = section.content

            updateExpandedState(section.isExpanded)

            headerLayout.setOnClickListener {
                section.isExpanded = !section.isExpanded
                updateExpandedState(section.isExpanded)
            }
        }

        private fun updateExpandedState(isExpanded: Boolean) {
            // Animate the expand/collapse icon rotation
            expandIcon.animate()
                .rotation(if (isExpanded) 180f else 0f)
                .setDuration(200)
                .start()

            // Toggle content visibility with fade animation
            if (isExpanded) {
                contentCard.visibility = View.VISIBLE
                contentCard.alpha = 0f
                contentCard.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            } else {
                contentCard.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        contentCard.visibility = View.GONE
                    }
                    .start()
            }
        }
    }
}