/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Aug 2, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Daniel M. Zimmerman <dmz@galois.com>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.model;

import static us.freeandfair.corla.util.EqualsHashcodeHelper.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Immutable;

import com.google.gson.annotations.JsonAdapter;

import us.freeandfair.corla.json.CVRContestInfoJsonAdapter;
import us.freeandfair.corla.persistence.PersistentEntity;

/**
 * A cast vote record contains information about a single ballot, either 
 * imported from a tabulator export file or generated by auditors.
 * 
 * @author Daniel M. Zimmerman
 * @version 0.0.1
 */
@Entity
@Immutable // this is a Hibernate-specific annotation, but there is no JPA alternative
@Cacheable(false) // since CVRs aren't cached, there is little point to caching these
@Table(name = "cvr_contest_info",
       indexes = { @Index(name = "idx_cvrci_cvr", columnList = "cvr_id"),
                   @Index(name = "idx_cvrci_cvr_contest", 
                          columnList = "cvr_id, contest_id")})
//this class has many fields that would normally be declared final, but
//cannot be for compatibility with Hibernate and JPA.
@SuppressWarnings("PMD.ImmutableField")
@JsonAdapter(CVRContestInfoJsonAdapter.class)
public class CVRContestInfo implements PersistentEntity, Serializable {
  /**
   * The serialVersionUID.
   */
  private static final long serialVersionUID = 1L;
  
  /**
   * The ID number.
   */
  @Id
  @Column(updatable = false, nullable = false)
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long my_id;
  
  /**
   * The version (for optimistic locking).
   */
  @Version
  private Long my_version;
  
  /**
   * The CVR to which this record belongs. 
   */
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn
  private CastVoteRecord my_cvr;
  
  /**
   * The contest in this record.
   */
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  private Contest my_contest;
  
  /** 
   * The comment for this contest.
   */
  @Column(updatable = false)
  private String my_comment;
  
  /**
   * The consensus value for this contest
   */
  @Column(updatable = false)
  @Enumerated(EnumType.STRING)
  private ConsensusValue my_consensus;
  
  /**
   * The choices for this contest.
   */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "cvr_contest_info_choice",
                   joinColumns = @JoinColumn(name = "cvr_contest_info_id", 
                                             referencedColumnName = "my_id"))
  @OrderColumn(name = "index")
  @Column(name = "choice")
  private List<String> my_choices = new ArrayList<>();

  /**
   * Constructs an empty CVRContestInfo, solely for persistence.
   */
  public CVRContestInfo() {
    super();
  }
  
  /**
   * Constructs a CVR contest information record with the specified 
   * parameters.
   * 
   * @param the_contest The contest.
   * @param the_comment The comment.
   * @param the_consensus The consensus value.
   * @param the_choices The choices.
   * @exception IllegalArgumentException if any choice is not a valid choice
   * for the specified contest.
   */
  public CVRContestInfo(final Contest the_contest, final String the_comment,
                        final ConsensusValue the_consensus,
                        final List<String> the_choices) {
    super();
    my_contest = the_contest;
    my_comment = the_comment;
    my_consensus = the_consensus;
    my_choices.addAll(the_choices);
    for (final String s : my_choices) {
      if (!my_contest.isValidChoice(s)) {
        throw new IllegalArgumentException("invalid choice " + s + 
                                           " for contest " + my_contest);
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Long id() {
    return my_id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setID(final Long the_id) {
    my_id = the_id;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Long version() {
    return my_version;
  }
  
  /**
   * Sets the CVR that owns this record; this should only be called by
   * the CastVoteRecord class.
   * 
   * @param the_cvr The CVR.
   */
  protected void setCVR(final CastVoteRecord the_cvr) {
    my_cvr = the_cvr;
  }

  /**
   * @return the CVR that owns this record.
   */
  public CastVoteRecord cvr() {
    return my_cvr;
  }
  
  /**
   * @return the contest in this record.
   */
  public Contest contest() {
    return my_contest;
  }
  
  /**
   * @return the comment in this record.
   */
  public String comment() {
    return my_comment;
  }
  
  /**
   * @return the consensus flag in this record.
   */
  public ConsensusValue consensus() {
    return my_consensus;
  }
  
  /**
   * @return the choices in this record.
   */
  public List<String> choices() {
    return Collections.unmodifiableList(my_choices);
  }
  
  /**
   * @return a String representation of this cast vote record.
   */
  @Override
  public String toString() {
    return "CVRContestInfo [contest=" + my_contest.id() + ", comment=" + 
           my_comment + ", consensus=" + my_consensus + ", choices=" +
           my_choices + "]";
  }
  
  /**
   * Compare this object with another for equivalence.
   * 
   * @param the_other The other object.
   * @return true if the objects are equivalent, false otherwise.
   */
  @Override
  public boolean equals(final Object the_other) {
    boolean result = true;
    if (the_other instanceof CVRContestInfo) {
      final CVRContestInfo other_info = (CVRContestInfo) the_other;
      result &= nullableEquals(other_info.contest(), contest());
      result &= nullableEquals(other_info.comment(), comment());
      result &= nullableEquals(other_info.consensus(), consensus());
      result &= nullableEquals(other_info.choices(), choices());
    } else {
      result = false;
    }
    return result;
  }
  
  /**
   * @return a hash code for this object.
   */
  @Override
  public int hashCode() {
    return nullableHashCode(choices());
  }

  /**
   * The possible values for consensus.
   */
  public enum ConsensusValue {
    YES,
    NO
  }
}
