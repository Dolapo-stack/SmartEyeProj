"""empty message

Revision ID: 956a17ff8b90
Revises: 
Create Date: 2020-08-17 17:38:38.061203

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = '956a17ff8b90'
down_revision = None
branch_labels = None
depends_on = None


def upgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.add_column('panic_history', sa.Column('fullname', sa.String(length=120), nullable=True))
    op.add_column('panic_history', sa.Column('user_id', sa.Integer(), nullable=True))
    op.create_index(op.f('ix_panic_history_user_id'), 'panic_history', ['user_id'], unique=False)
    op.create_foreign_key(None, 'panic_history', 'users', ['user_id'], ['id'], ondelete='CASCADE')
    op.add_column('users', sa.Column('dob', sa.String(length=255), nullable=True))
    # ### end Alembic commands ###


def downgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.drop_column('users', 'dob')
    op.drop_constraint(None, 'panic_history', type_='foreignkey')
    op.drop_index(op.f('ix_panic_history_user_id'), table_name='panic_history')
    op.drop_column('panic_history', 'user_id')
    op.drop_column('panic_history', 'fullname')
    # ### end Alembic commands ###
